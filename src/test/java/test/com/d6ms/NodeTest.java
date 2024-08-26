package test.com.d6ms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.d6ms.dto.NodeInfo;
import com.d6ms.dto.NodeSearchCriteria;
import com.d6ms.type.NodeType;
import com.d6ms.utils.DmsUtils;

public class NodeTest extends BaseTest {

	@Test
	public void testAddingFolderByName() throws Exception {
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

	@Test
	public void testAddingFolderAndDocumentsRecursivelyByFileSystem() throws Exception {

		List<NodeInfo> nodeInfos;

		{
			String bk = "bk11";
			String folderPath = "./test-data/d1";

			File d = new File(folderPath);

			String id = trxService.execute(() -> dmsService.saveFolderNode(storeId, null, bk, d, (f) -> true));

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
				criteria.setType(NodeType.FOLDER);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(3, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.DOCUMENT);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(2, nodeInfos.size());
			}

			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.FOLDER);
				criteria.setName("d1-1");
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(1, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.DOCUMENT);
				criteria.setName("d1-1");
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(0, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.FOLDER);
				criteria.setName("d1-1-f1.txt");
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(0, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.DOCUMENT);
				criteria.setName("d1-1-f1.txt");
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(1, nodeInfos.size());

				String documentId = nodeInfos.get(0).getId();

				byte[] content = trxService.execute(() -> dmsService.loadContent(documentId));

				assertEquals("d1-1-f1.txt content", new String(content));
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

			String id = trxService.execute(() -> dmsService.saveFolderNode(storeId, null, bk, d, (f) -> true));

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
				criteria.setType(NodeType.FOLDER);
				nodeInfos = dmsService.loadNodeInfos(criteria);
				assertEquals(3, nodeInfos.size());
			}
			{
				NodeSearchCriteria criteria = new NodeSearchCriteria();
				criteria.setBusinessKeys(List.of(bk));
				criteria.setStoreId(storeId);
				criteria.setType(NodeType.DOCUMENT);
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

	}

}
