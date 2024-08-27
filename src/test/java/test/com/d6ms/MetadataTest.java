package test.com.d6ms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.d6ms.dto.NodeInfo;
import com.d6ms.dto.NodeSearchCriteria;

public class MetadataTest extends BaseTest {

	@Test
	public void testAddingFolderByName() throws Exception {
		String bk = "bk11";
		String content = "Hello Content 1";

		int nb = 1;

		String id = trxService
				.execute(() -> dmsService.saveDocumentNode(storeId, null, "mt1", "mi1", bk, "Content1", content));

		{
			assertNotNull(id);
			NodeSearchCriteria criteria = new NodeSearchCriteria();
			criteria.setStoreId(storeId);
			List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);
			assertEquals(nb, nodeInfos.size());
		}

		{
			Map<String, String> metadata = new HashMap<>();
			metadata.put("m1", "v1");
			metadata.put("m2", "v2");

			trxService.execute(() -> dmsService.updateMetadata(id, metadata, true));

			NodeSearchCriteria criteria = new NodeSearchCriteria();
			criteria.setStoreId(storeId);
			criteria.setLoadMetadata(false);
			criteria.setIds(List.of(id));

			List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);
			assertEquals(nb, nodeInfos.size());
			NodeInfo node = nodeInfos.get(0);
			assertNull(node.getIndexes());

			criteria.setLoadMetadata(true);

			nodeInfos = dmsService.loadNodeInfos(criteria);
			assertEquals(nb, nodeInfos.size());
			node = nodeInfos.get(0);
			assertNotNull(node.getIndexes());

			assertEquals(2, node.getIndexes().size());

			assertEquals("v1", node.getIndexes().get("m1"));
			assertEquals("v2", node.getIndexes().get("m2"));

			Map<String, String> metadata2 = new HashMap<>();
			metadata2.put("m1", "v1-newval");
			metadata2.put("m3", "v3");

			trxService.execute(() -> dmsService.updateMetadata(id, metadata2, false));

			nodeInfos = dmsService.loadNodeInfos(criteria);
			assertEquals(nb, nodeInfos.size());
			node = nodeInfos.get(0);
			assertNotNull(node.getIndexes());

			assertEquals(3, node.getIndexes().size());

			assertEquals("v1-newval", node.getIndexes().get("m1"));
			assertEquals("v2", node.getIndexes().get("m2"));
			assertEquals("v3", node.getIndexes().get("m3"));

			Map<String, String> metadata3 = new HashMap<>();
			metadata3.put("m1", "v1-newval2");
			metadata3.put("m4", "v4");

			trxService.execute(() -> dmsService.updateMetadata(id, metadata3, true));

			nodeInfos = dmsService.loadNodeInfos(criteria);
			assertEquals(nb, nodeInfos.size());
			node = nodeInfos.get(0);
			assertNotNull(node.getIndexes());

			assertEquals(2, node.getIndexes().size());

			assertEquals("v1-newval2", node.getIndexes().get("m1"));
			assertEquals("v4", node.getIndexes().get("m4"));
		}

	}

}
