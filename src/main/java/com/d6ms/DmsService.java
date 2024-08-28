package com.d6ms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d6ms.dto.NodeInfo;
import com.d6ms.dto.NodeSearchCriteria;
import com.d6ms.dto.NodeTreeElement;
import com.d6ms.entity.Metadata;
import com.d6ms.entity.Node;
import com.d6ms.entity.NodeContent;
import com.d6ms.entity.Store;
import com.d6ms.type.ActionType;
import com.d6ms.type.NodeType;
import com.d6ms.type.State;

public class DmsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DmsService.class);

	public static final Charset ENCODING = StandardCharsets.UTF_8;

	private DmsRepo dmsRepo;

	public DmsService(DmsRepo dmsRepo) {
		super();
		this.dmsRepo = dmsRepo;
	}

	public List<NodeInfo> loadNodeInfos(NodeSearchCriteria criteria) {
		return dmsRepo.loadNodeInfos(criteria);
	}

	public byte[] loadContent(String id) {
		LOGGER.info("Loading content {} ..", id);

		Node d = dmsRepo.getNode(id);
		byte[] t = d.getNodeContent().getContent();
		return Arrays.copyOf(t, t.length);
	}

	public void loadContent(String id, OutputStream out) throws IOException {
		byte[] t = loadContent(id);
		out.write(t);
	}

	public void loadContent(String id, File outFile) throws IOException {
		byte[] t = loadContent(id);
		FileUtils.writeByteArrayToFile(outFile, t);
	}

	public String saveDir(String storeId, String parentNodeId, String businesskey, File folderFile,
			Function<File, Boolean> filter) throws Exception {

		String id = saveDir(storeId, parentNodeId, businesskey, folderFile.getName());

		File[] files = folderFile.listFiles();

		if (files != null) {
			for (File f : files) {
				if (filter != null) {
					Boolean inc = filter.apply(f);
					if (inc != null && inc) {
						if (f.isDirectory()) {
							saveDir(storeId, id, businesskey, f, filter);
						} else {
							saveFile(storeId, id, File.class.getCanonicalName(), f.getCanonicalPath(), businesskey,
									null, f.getName(), f);
						}
					}
				}
			}
		}

		List<NodeTreeElement> tree = dmsRepo.getHierarchy(List.of(id));

		dmsRepo.saveAction(id, ActionType.CREATION, tree, LocalDateTime.now());

		return id;
	}

	public String saveDir(String storeId, String parentNodeId, String businesskey, String name) throws IOException {

		LOGGER.info("#folder# Saving node [store={}, parent={}, bk={}, name={}]", storeId, parentNodeId, businesskey,
				name);

		ObjectUtils.requireNonEmpty(storeId, "Store Id");
		ObjectUtils.requireNonEmpty(storeId, "Name");

		Store store = new Store(storeId);

		Node n = new Node();
		n.setBusinessKey(StringUtils.defaultIfBlank(businesskey, null));
		n.setName(StringUtils.trim(name));
		n.setStore(store);
		n.setType(NodeType.DIR);
		n.setState(State.ACTIVE);

		if (parentNodeId != null) {
			Node pf = new Node();
			pf.setId(parentNodeId);
			pf.setStore(store);
			n.setParent(pf);
		}

		dmsRepo.save(n);

		String id = n.getId();

		return id;
	}

	public void archive(String id) {
		if (StringUtils.isBlank(id)) {
			return;
		}

		List<NodeTreeElement> tree = dmsRepo.archive(id);

		if (!tree.isEmpty()) {
			dmsRepo.saveAction(id, ActionType.ARCHIVING, tree, LocalDateTime.now());
		}
	}

	public void rename(String id, String name) {
		dmsRepo.rename(id, name);
	}

	public List<NodeTreeElement> getHierarchy(String storeId) {
		return dmsRepo.getHierarchy(storeId);
	}

	public List<NodeTreeElement> getHierarchy(Collection<String> rootIds) {
		return dmsRepo.getHierarchy(rootIds);
	}

	public String saveFile(String storeId, String folderId, String masterType, String masterId, String businesskey,
			String category, String name, Object o) throws IOException, Exception {

		LOGGER.info("#file# Saving node [store={}, parent={}, masterType={}, masterId={}, bk={}, category={}, name={}]",
				storeId, folderId, masterType, masterId, businesskey, category, name);

		ObjectUtils.requireNonEmpty(storeId, "Store Id");
		ObjectUtils.requireNonEmpty(storeId, "Name");
		ObjectUtils.requireNonEmpty(o, "Content");

		byte[] content = null;

		if (o instanceof CharSequence e) {
			content = e.toString().getBytes(ENCODING);
		} else if (o instanceof File e) {
			content = FileUtils.readFileToByteArray(e);
		} else if (o instanceof URL e) {
			content = IOUtils.toByteArray(e);
		} else if (o instanceof InputStream e) {
			content = IOUtils.toByteArray(e);
		} else if (o instanceof Reader e) {
			content = IOUtils.toString(e).getBytes(ENCODING);
		} else if (o instanceof byte[] e) {
			content = e;
		} else {
			throw new IllegalArgumentException("Unmanaged content : " + o.getClass().getCanonicalName());
		}

		long length = content.length;

		String contentType = new Tika().detect(content);

		Store store = new Store();
		store.setId(storeId);

		NodeContent nc = new NodeContent();
		nc.setContent(content);
		nc.setStore(store);
		dmsRepo.save(nc);

		Node n = new Node();
		n.setCategory(StringUtils.defaultIfBlank(category, null));
		n.setBusinessKey(StringUtils.defaultIfBlank(businesskey, null));
		n.setName(name);
		n.setMasterId(StringUtils.defaultIfBlank(masterId, null));
		n.setMasterType(StringUtils.defaultIfBlank(masterType, null));
		n.setStore(store);
		n.setSize(length);
		n.setType(NodeType.FILE);
		n.setContentType(contentType);
		n.setState(State.ACTIVE);
		n.setNodeContent(nc);

		if (!StringUtils.isBlank(folderId)) {
			Node pf = new Node();
			pf.setId(folderId);
			pf.setStore(store);
			n.setParent(pf);
		}

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(content);
		String md5 = Hex.encodeHexString(md.digest()).toLowerCase();
		n.setMd5(md5);

		dmsRepo.save(n);

		String id = n.getId();

		return id;
	}

	public int updateMetadata(String id, Map<String, String> indexMap, boolean sync) {
		List<Metadata> metadataList = dmsRepo.getNodeMetadata(id);

		Node d = new Node();
		d.setId(id);

		List<Metadata> updatedMetadataList = createUpdateMetadataList(metadataList, indexMap, sync);

		for (Metadata m : updatedMetadataList) {
			m.setNode(d);
		}

		dmsRepo.save(updatedMetadataList);

		return updatedMetadataList.size();

	}

	public void updateNodeMasterInfos(String documentId, String masterId, String masterType) {
		Node d = dmsRepo.getNode(documentId);
		if (d != null) {
			d.setMasterId(StringUtils.trim(masterId));
			d.setMasterType(StringUtils.trim(masterType));
			dmsRepo.save(d);
		}
	}

	public void updateNodeBusinessKey(String id, String businessKey) {
		Node d = dmsRepo.getNode(id);
		if (d != null) {
			d.setBusinessKey(StringUtils.trim(businessKey));
			dmsRepo.save(d);
		}
	}

	private List<Metadata> createUpdateMetadataList(List<Metadata> metadataList, Map<String, String> metadataMap,
			boolean sync) {

		Map<String, Metadata> map = new HashMap<>();

		List<Metadata> updatedIndexes = new ArrayList<>();

		if (sync) {
			for (Metadata m : metadataList) {

				String name = m.getName().trim();

				if (!metadataMap.containsKey(name)) {
					m.setState(State.ARCHIVED);
					updatedIndexes.add(m);
				} else if (m.getState() != State.ARCHIVED) {
					String newValue = metadataMap.get(name);

					if (newValue == null) {
						newValue = "";
					}
					newValue = newValue.trim();

					if (!StringUtils.equals(m.getValue(), newValue)) {
						m.setValue(newValue);
						updatedIndexes.add(m);
					}
				}
			}
		} else {
			for (String name : metadataMap.keySet()) {

				name = name.trim();

				Metadata m = map.get(name);

				if (m != null) {
					String newValue = metadataMap.get(name);
					if (newValue == null) {
						newValue = "";
					}
					newValue = newValue.trim();

					m = new Metadata();
					m.setState(State.ACTIVE);
					m.setName(name);
					m.setValue(newValue);
					updatedIndexes.add(m);
				}
			}
		}

		for (String name : metadataMap.keySet()) {

			name = name.trim();

			Metadata m = map.get(name);

			if (m == null) {
				String newValue = metadataMap.get(name);
				if (newValue == null) {
					newValue = "";
				}
				newValue = newValue.trim();

				m = new Metadata();
				m.setState(State.ACTIVE);
				m.setName(name);
				m.setValue(newValue);
				updatedIndexes.add(m);
			}
		}

		return updatedIndexes;
	}

}
