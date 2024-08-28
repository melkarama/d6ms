package test.com.d6ms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.d6ms.dto.NodeInfo;
import com.d6ms.dto.NodeSearchCriteria;
import com.d6ms.dto.NodeTreeElement;
import com.d6ms.type.NodeType;
import com.d6ms.utils.DmsUtils;
import com.d6ms.utils.Utils;

public class NodeTest extends BaseTest {

	@Test
	public void testAddingDirByName() throws Exception {
		String bk = "bk01";

		int nb = 3;

		for (int i = 1; i <= nb; i++) {
			String name = "Dir " + i;
			String id = trxService.execute(() -> dmsService.saveDir(storeId, null, bk, name));
			assertNotNull(id);

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setIds(List.of(id));
				criteria.setStoreId(storeId);
				List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(1, nodeInfos.size());
			}

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setIds(List.of(id));
				criteria.setStoreId(storeId);
				List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(1, nodeInfos.size());
			}

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setStoreId(storeId);
				List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(i, nodeInfos.size());
			}

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setStoreId(storeId);
				criteria.setBusinessKeys(List.of(bk));
				List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(i, nodeInfos.size());
			}
		}

		NodeSearchCriteria criteria = new NodeSearchCriteria();
		criteria.setStoreId(storeId);

		List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);

		assertEquals(nb, nodeInfos.size());
	}

	@Test
	public void testAddingDirAndFilesRecursivelyByFileSystem() throws Exception {

		List<NodeInfo> nodeInfos;

		{
			String bk = "bk11";
			String folderPath = "./test-data/d1";

			File d = new File(folderPath);

			String id = trxService.execute(() -> dmsService.saveDir(storeId, null, bk, d, (f) -> true));

			assertNotNull(id);

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(5, nodeInfos.size());
			}

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.DIR);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(3, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.FILE);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(2, nodeInfos.size());
			}
		}

		{
			NodeSearchCriteria criteria = new NodeSearchCriteria();
			criteria.setStoreId(storeId);
			nodeInfos = dmsService.loadNodeInfos(criteria);
			assertEquals(1, DmsUtils.getRootNodes(nodeInfos).size());
		}

		{
			String bk = "bk12";
			String folderPath = "./test-data/d2";

			File d = new File(folderPath);

			String id = trxService.execute(() -> dmsService.saveDir(storeId, null, bk, d, (f) -> true));

			assertNotNull(id);

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(6, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.DIR);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(3, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.FILE);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(3, nodeInfos.size());
			}
		}

		{
			NodeSearchCriteria criteria = new NodeSearchCriteria();
			criteria.setStoreId(storeId);
			nodeInfos = dmsService.loadNodeInfos(criteria);
			assertEquals(2, DmsUtils.getRootNodes(nodeInfos).size());
		}

		List<NodeTreeElement> tree = dmsRepo.getHierarchy(storeId);

		System.out.println(Utils.join(tree, "\n", (e) -> e.print()));
	}

	@Test
	public void testAddingInlineContent() throws Exception {
		String bk = "bk03";
		String content = "Hello Content 1";

		int nb = 1;

		String id = trxService
				.execute(() -> dmsService.saveFile(storeId, null, "mt1", "mi1", bk, null, "Content1", content));

		assertNotNull(id);

		NodeSearchCriteria criteria = new NodeSearchCriteria();
		criteria.setStoreId(storeId);

		List<NodeInfo> nodeInfos = dmsService.loadNodeInfos(criteria);

		assertEquals(nb, nodeInfos.size());

		byte[] t = trxService.execute(() -> dmsService.loadContent(id));

		assertNotNull(t);

		assertEquals(content, new String(t, StandardCharsets.UTF_8));
	}

}
