package test.com.d6ms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.d6ms.dto.NodeInfo;
import com.d6ms.dto.NodeSearchCriteria;

public class NodeTest extends BaseTest {

	@Test
	public void test() throws Exception {
		String bk = "bk01";

		int nb = 3;

		for (int i = 1; i <= nb; i++) {
			String name = "Folder " + i;
			String id = trxService.execute(() -> dmsService.saveFolderNode(storeId, null, bk, name));
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

}
