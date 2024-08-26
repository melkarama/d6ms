package com.d6ms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.d6ms.dto.NodeInfo;
import com.d6ms.dto.NodeSearchCriteria;
import com.d6ms.dto.NodeTreeElement;
import com.d6ms.entity.Action;
import com.d6ms.entity.BaseEntity;
import com.d6ms.entity.Metadata;
import com.d6ms.entity.Node;
import com.d6ms.type.ActionType;
import com.d6ms.type.NodeType;
import com.d6ms.type.State;
import com.d6ms.utils.Utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

public class DmsRepo {

	private EntityManager em;

	public DmsRepo(EntityManager em) {
		super();
		this.em = em;
	}

	public Node getNode(String id) {
		String sql = "select e from Node e where e.id = :id";
		Map<String, Object> params = Map.of("id", id);

		try {
			TypedQuery<Node> q = em.createQuery(sql, Node.class);
			applyParams(q, params);

			return q.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}

	public List<Node> getNodes(Collection<String> ids) {
		String sql = "select e from Node e where e.id in (:ids)";
		Map<String, Object> params = Map.of("ids", ids);

		TypedQuery<Node> q = em.createQuery(sql, Node.class);
		applyParams(q, params);

		return q.getResultList();
	}

	public void save(BaseEntity e) {
		if (e.getId() == null) {
			em.persist(e);
		} else {
			em.merge(e);
		}
	}

	public void save(Collection<? extends BaseEntity> t) {
		for (BaseEntity e : t) {
			save(e);
		}
	}

	public <E extends BaseEntity> List<E> load(Class<E> clazz, Collection<String> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<>();
		}
		return em.createQuery("select e from " + clazz.getCanonicalName() + " e where e.id in (:ids)", clazz)
				.setParameter("ids", ids).getResultList();
	}

	public List<Metadata> getNodeMetadata(String id) {
		Map<String, List<Metadata>> idxMap = getNodeMetadata(List.of(id));

		List<Metadata> metadataList = idxMap.get(id);

		return ObjectUtils.defaultIfNull(metadataList, new ArrayList<>());
	}

	public Map<String, List<Metadata>> getNodeMetadata(Collection<String> ids) {
		String sql = "select d.id, m from Metadata m join m.node d where d.id in (:ids)";
		Map<String, Object> params = Map.of("ids", ids);

		TypedQuery<Object[]> q = em.createQuery(sql, Object[].class);
		applyParams(q, params);

		List<Object[]> sqlResults = q.getResultList();

		Map<String, List<Metadata>> resultMap = new LinkedHashMap<>();

		for (Object[] t : sqlResults) {
			String id = (String) t[0];
			Metadata m = (Metadata) t[1];

			List<Metadata> t2 = resultMap.get(id);
			if (t2 == null) {
				t2 = new ArrayList<>();
				resultMap.put(id, t2);
			}
			t2.add(m);
		}

		return resultMap;
	}

	public List<NodeTreeElement> archiveNode(String id) {

		if (StringUtils.isBlank(id)) {
			return new ArrayList<>();
		}

		return archiveNodes(List.of(id));
	}

	public List<NodeTreeElement> archiveNodes(Collection<String> ids) {

		if (ids == null || ids.isEmpty()) {
			return new ArrayList<>();
		}

		List<NodeTreeElement> tree = Utils.flatten(getHierarchy(ids));

		List<String> treeIds = tree.stream().map(NodeTreeElement::getId).collect(Collectors.toList());
		List<List<String>> treeIds2 = ListUtils.partition(treeIds, 999);

		for (List<String> treeIds3 : treeIds2) {
			Map<String, Object> params = Map.of("ids", treeIds3, "state", State.ARCHIVED);
			{
				String sql = "update Node e set e.state = :state where e.id in (:ids)";
				Query q = em.createQuery(sql);
				applyParams(q, params);
				q.executeUpdate();
			}
			{
				String sql = "update Metadata e set e.state = :state where e.node.id in (:ids)";
				Query q = em.createQuery(sql);
				applyParams(q, params);
				q.executeUpdate();
			}
		}

		return tree;
	}

	public void saveAction(String id, ActionType type, List<NodeTreeElement> tree, LocalDateTime dt) {
		Action e = new Action();
		e.setArchiveDate(dt);
		e.setType(type);
		e.setNodeId(id);
		e.setTargetHierarchy(Utils.toJson(tree, true));
		save(e);
	}

	public List<NodeInfo> loadNodeInfos(NodeSearchCriteria criteria) {

		Map<String, String> idxMap = criteria.getIndexes() == null ? new HashMap<>()
				: new TreeMap<>(criteria.getIndexes());

		String sql = "select e from Node e join e.store s left join e.parent p ";

		for (int idx = 0; idx < idxMap.size(); idx++) {
			String idx_alias = "metadata_" + idx;
			sql = appendSql(sql, "join Metadata " + idx_alias + " on " + idx_alias + ".node.id = e.id", null);
		}

		sql += "\n where 1=1";

		Map<String, Object> params = new HashMap<>();

		sql = appendSql(sql, "e.store.id = :storeId", params, "storeId", criteria.getStoreId());

		if (criteria.getIds() != null && !criteria.getIds().isEmpty()) {
			sql = appendSql(sql, "e.id in (:ids)", params, "ids", criteria.getIds());
		}

		if (!StringUtils.isBlank(criteria.getParentId())) {
			sql = appendSql(sql, "p.id = :parentId", params, "parentId", criteria.getParentId());
		}

		if (criteria.getBusinessKeys() != null && !criteria.getBusinessKeys().isEmpty()) {
			sql = appendSql(sql, "e.businessKey in (:businessKeys)", params, "businessKeys",
					criteria.getBusinessKeys());
		}

		if (criteria.getMasterIds() != null && !criteria.getMasterIds().isEmpty()) {
			sql = appendSql(sql, "e.masterId in (:masterIds)", params, "masterIds", criteria.getMasterIds());
		}

		if (criteria.getMasterTypes() != null && !criteria.getMasterTypes().isEmpty()) {
			sql = appendSql(sql, "e.masterType in (:masterTypes)", params, "masterTypes", criteria.getMasterTypes());
		}

		if (criteria.getStates() != null && !criteria.getStates().isEmpty()) {
			sql = appendSql(sql, "e.state in (:states)", params, "states", criteria.getStates());
		}

		if (criteria.getType() != null) {
			sql = appendSql(sql, "e.type = :type", params, "type", criteria.getType());
		}

		int idx = 0;
		for (String idxName : idxMap.keySet()) {
			String idxValue = idxMap.get(idxName);

			String idx_alias = "metadata_" + idx;
			String idx_param_name = "metadata_name_" + idx;
			String idx_param_value = "metadata_value_" + idx;

			sql = appendSql(sql,
					idx_alias + ".name = :" + idx_param_name + " and " + idx_alias + ".value = :" + idx_param_value,
					params, idx_param_name, idxName, idx_param_value, idxValue);

			idx++;
		}

		TypedQuery<Node> q = em.createQuery(sql, Node.class);
		applyParams(q, params);
		applyPagination(q, criteria.getPageSize(), criteria.getPageNumber());

		List<Node> nodes = q.getResultList();

		Map<String, NodeInfo> nodeMap = new LinkedHashMap<>();

		for (Node n : nodes) {
			NodeInfo dmsInfo = createNodeInfo(n);
			nodeMap.put(dmsInfo.getId(), dmsInfo);
		}

		if (criteria.isLoadMetadata()) {
			Map<String, List<Metadata>> metadataMap = getNodeMetadata(nodeMap.keySet());

			for (NodeInfo e : nodeMap.values()) {
				List<Metadata> metadata = metadataMap.get(e.getId());
				e.setIndexes(createIndexMap(metadata));
			}
		}

		return new ArrayList<>(nodeMap.values());
	}

	public List<NodeTreeElement> getHierarchy(Collection<String> rootIds) {
		String sql = """
				WITH RECURSIVE node_tree (id, name, type, parent_id, store_id, level) AS (
				   SELECT id, name, type, parent_id, store_id, 1 AS level
				   FROM dms_node """;

		if (rootIds == null || rootIds.isEmpty()) {
			sql += "\n WHERE parent_id is null ";
		} else {
			sql += "\n WHERE id in (?) ";
		}

		sql += """
				    UNION ALL
				    SELECT p.id, p.name, p.type, p.parent_id, p.store_id, pt.level + 1 AS level
				    FROM dms_node p
				    INNER JOIN node_tree pt ON p.parent_id = pt.id
				)
				SELECT * FROM node_tree
						""";

		Query q = em.createNativeQuery(sql, Object[].class);
		q.setParameter(1, rootIds);

		@SuppressWarnings("unchecked")
		List<Object[]> lines = q.getResultList();

		Map<String, NodeTreeElement> cacheMap = new HashMap<>(1000);

		for (Object[] t : lines) {
			String id = (String) t[0];
			String name = (String) t[1];
			NodeType type = NodeType.valueOf((String) t[2]);
			String parentId = (String) t[3];
			String storeId = (String) t[4];
			int level = NumberUtils.toInt("" + t[4]);

			NodeTreeElement e = new NodeTreeElement();

			e.setId(storeId);
			e.setName(name);
			e.setStoreId(storeId);
			e.setParentId(parentId);
			e.setType(type);
			e.setLevel(level);

			cacheMap.put(id, e);

			NodeTreeElement p = cacheMap.get(parentId);

			if (p != null) {
				if (p.getChildren() == null) {
					p.setChildren(new HashMap<>());
				}
				p.getChildren().put(e.getId(), e);
			}
		}

		List<NodeTreeElement> results = new ArrayList<>();

		for (NodeTreeElement e : cacheMap.values()) {
			if ((rootIds == null || rootIds.isEmpty()) && StringUtils.isBlank(e.getParentId())
					|| rootIds.contains(e.getId())) {
				results.add(e);
			}
		}

		return results;
	}

	private void applyParams(Query q, Map<String, ?> params) {
		if (params != null) {
			for (String name : params.keySet()) {
				q.setParameter(name, params.get(name));
			}
		}
	}

	private void applyPagination(Query q, int pageSize, int pageNumber) {
		if (pageSize > 0 && pageNumber >= 0) {
			q.setFirstResult(pageSize * pageNumber);
			q.setMaxResults(pageSize);
		}
	}

	private String appendSql(String sql, String chunk, Map<String, Object> params, Object... chunkParams) {
		String sql2 = sql + "\n and " + chunk;
		if (chunkParams != null && chunkParams.length > 0) {
			for (int i = 0; i < chunkParams.length; i += 2) {
				params.put((String) chunkParams[i], chunkParams[i + 1]);
			}
		}
		return sql2;
	}

	private Map<String, String> createIndexMap(List<Metadata> metadata) {

		Map<String, String> resultMap = new TreeMap<>();

		if (metadata != null) {
			for (Metadata m : metadata) {
				resultMap.put(m.getName(), m.getValue());
			}
		}

		return resultMap;
	}

	private NodeInfo createNodeInfo(Node n) {
		NodeInfo e = new NodeInfo();

		e.setId(n.getId());
		e.setName(n.getName());
		e.setContentType(n.getContentType());
		e.setSize(n.getSize());
		e.setMd5(n.getMd5());
		e.setParentId(n.getParent() == null ? null : n.getParent().getId());
		e.setStoreId(n.getStore().getId());
		e.setMasterId(n.getMasterId());
		e.setMasterType(n.getMasterType());
		e.setBusinessKey(n.getBusinessKey());
		e.setType(n.getType());

		return e;
	}
}
