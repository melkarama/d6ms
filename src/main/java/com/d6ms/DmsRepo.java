package com.d6ms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.d6ms.type.DatabaseType;
import com.d6ms.type.NodeType;
import com.d6ms.type.State;
import com.d6ms.utils.Utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

public class DmsRepo {

	private EntityManager em;

	private DatabaseType dbType;

	public DmsRepo(EntityManager em, DatabaseType dbType) {
		super();
		this.em = em;
		this.dbType = dbType;
	}

	public Node rename(String id, String name) {

		ObjectUtils.requireNonEmpty(id, "Node Id");
		ObjectUtils.requireNonEmpty(name, "Name");

		Node e = load(Node.class, id);

		if (e != null) {
			e.setName(StringUtils.trim(name));

			e = em.merge(e);
		}

		return e;
	}

	public Node getNode(String id) {
		ObjectUtils.requireNonEmpty(id, "Node id");

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
		ObjectUtils.requireNonEmpty(e, "Entity");

		if (e.getId() == null) {
			em.persist(e);
		} else {
			em.merge(e);
		}
	}

	public void save(Collection<? extends BaseEntity> t) {
		ObjectUtils.requireNonEmpty(t, "Entity List");

		for (BaseEntity e : t) {
			ObjectUtils.requireNonEmpty(e, "Entity");

			save(e);
		}
	}

	public <E extends BaseEntity> E load(Class<E> clazz, String id) {
		ObjectUtils.requireNonEmpty(id, "Id");

		try {
			return em.find(clazz, id);
		} catch (NoResultException ex) {
			return null;
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
		ObjectUtils.requireNonEmpty(id, "Node id");

		Map<String, List<Metadata>> idxMap = getNodeMetadata(List.of(id));

		List<Metadata> metadataList = idxMap.get(id);

		return ObjectUtils.defaultIfNull(metadataList, new ArrayList<>());
	}

	public Map<String, List<Metadata>> getNodeMetadata(Collection<String> ids) {
		String sql = "select m from Metadata m join fetch m.node n where n.id in (:ids) ";
		sql += " and n.state = :activeState and m.state = :activeState";
		Map<String, Object> params = Map.of("ids", ids, "activeState", State.ACTIVE);

		TypedQuery<Metadata> q = em.createQuery(sql, Metadata.class);
		applyParams(q, params);

		List<Metadata> sqlResults = q.getResultList();

		Map<String, List<Metadata>> resultMap = new LinkedHashMap<>();

		for (Metadata m : sqlResults) {
			String nodeId = m.getNode().getId();
			List<Metadata> t2 = resultMap.get(nodeId);
			if (t2 == null) {
				t2 = new ArrayList<>();
				resultMap.put(nodeId, t2);
			}
			t2.add(m);
		}

		return resultMap;
	}

	public List<NodeTreeElement> archive(String id) {

		ObjectUtils.requireNonEmpty(id, "Node id");

		if (StringUtils.isBlank(id)) {
			return new ArrayList<>();
		}

		return archive(List.of(id));
	}

	public List<NodeTreeElement> archive(Collection<String> ids) {

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
		String targt = Utils.toJson(tree, true);

		Action e = new Action();
		e.setArchiveDate(dt);
		e.setType(type);
		e.setNodeId(id);
		e.setTargetHierarchy(targt);
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

		if (!StringUtils.isBlank(criteria.getName())) {
			sql = appendSql(sql, "e.name = :name", params, "name", criteria.getName());
		}

		if (criteria.getMasterMap() != null) {
			String sql2 = "";

			int idx = 0;
			for (Entry<String, Collection<String>> e : criteria.getMasterMap().entrySet()) {
				String masterType = StringUtils.trim(e.getKey());
				List<String> masterIds = filterIds(e.getValue());

				if (StringUtils.isBlank(masterType) && masterIds.isEmpty()) {
					continue;
				}

				if (sql2.length() > 0) {
					sql2 += "\n or ";
				}

				sql2 += "(";

				if (!StringUtils.isBlank(masterType)) {
					String masterTypeParam = "master_map_type_" + idx;
					sql2 += " e.masterType = :" + masterTypeParam;
					params.put(masterTypeParam, masterType);

					if (!masterIds.isEmpty()) {
						String masterIdParam = "master_map_id_" + idx;
						sql2 += " and e.masterId in (:" + masterIdParam + ")";
						params.put(masterIdParam, StringUtils.join(masterIds, ", "));

					}
				} else if (!masterIds.isEmpty()) {
					String masterIdParam = "master_map_id_" + idx;
					sql2 += " e.masterId in (:" + masterIdParam + ")";
					params.put(masterIdParam, StringUtils.join(masterIds, ", "));
				}

				sql2 += ")";

				idx++;
			}

			if (sql2.length() > 0) {
				sql += "\n and " + sql2;
			}
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

	public List<NodeTreeElement> getHierarchy(String storeId) {
		return getHierarchy(storeId, null);
	}

	public List<NodeTreeElement> getHierarchy(Collection<String> rootIds) {
		return getHierarchy(null, rootIds);
	}

	private List<NodeTreeElement> getHierarchy(String storeId, Collection<String> rootIds) {

		rootIds = filterIds(rootIds);

		if (rootIds.isEmpty()) {
			ObjectUtils.requireNonEmpty(storeId, "Store Id");
		}

		Query q = createHierarchyQuery(storeId, rootIds);

		@SuppressWarnings("unchecked")
		List<Object[]> lines = q.getResultList();

		Map<String, NodeTreeElement> cacheMap = new HashMap<>(1000);

		for (Object[] t : lines) {
			String id = (String) t[0];
			String name = (String) t[1];
			NodeType type = NodeType.valueOf((String) t[2]);
			String parentId = (String) t[3];
			String storeId2 = (String) t[4];
			int level = NumberUtils.toInt("" + t[5]);

			NodeTreeElement e = new NodeTreeElement();

			e.setId(id);
			e.setName(name);
			e.setStoreId(storeId2);
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
			if (e.getParentId() == null) {
				results.add(e);
			}
		}

		return results;
	}

	private Query createHierarchyQuery(String storeId, Collection<String> rootIds) {
		rootIds = filterIds(rootIds);

		String sql;
		Object[] params;

		if (dbType == DatabaseType.ORACLE) {
			sql = """
					SELECT id,
					      name,
					      type,
					      parent_id,
					      store_id,
					      level
					FROM dms_node
					""";

			if (rootIds.isEmpty()) {
				sql += "\n START WITH store_id = ? and parent_id is null ";
			} else if (!StringUtils.isBlank(storeId)) {
				sql += "\n START WITH store_id = ? and id in (?)";
			} else {
				sql += "\n START WITH id in (?)";
			}

			sql += """
					CONNECT BY PRIOR id = parent_id
					ORDER SIBLINGS BY id
										""";

			params = rootIds.isEmpty() ? new Object[] { storeId, }
					: StringUtils.isBlank(storeId) ? new Object[] { rootIds } : new Object[] { storeId, rootIds };

		} else {

			sql = """
					WITH RECURSIVE node_tree (id, name, type, parent_id, store_id, level) AS (
					   SELECT id, name, type, parent_id, store_id, 1 AS level
					   FROM dms_node """;

			if (rootIds.isEmpty()) {
				sql += "\n WHERE store_id = ? and parent_id is null ";
			} else if (!StringUtils.isBlank(storeId)) {
				sql += "\n WHERE store_id = ? and id is (?) ";
			} else {
				sql += "\n WHERE id in (?) ";
			}

			sql += """
					    \n UNION ALL
					    SELECT p.id, p.name, p.type, p.parent_id, p.store_id, pt.level + 1 AS level
					    FROM dms_node p
					    INNER JOIN node_tree pt ON p.parent_id = pt.id
					""";

			if (!StringUtils.isBlank(storeId)) {
				sql += "\n WHERE p.store_id = ?";
			}

			sql += """
					)
					SELECT * FROM node_tree
					""";

			params = rootIds.isEmpty() ? new Object[] { storeId, storeId }
					: StringUtils.isBlank(storeId) ? new Object[] { rootIds }
							: new Object[] { storeId, rootIds, storeId };
		}

		Query q = em.createNativeQuery(sql, Object[].class);
		applyParams(q, params);

		return q;
	}

	private void applyParams(Query q, Map<String, ?> params) {
		if (params != null) {
			for (String name : params.keySet()) {
				q.setParameter(name, params.get(name));
			}
		}
	}

	private void applyParams(Query q, Object... params) {
		if (params != null) {
			for (int i = 1; i <= params.length; i++) {
				q.setParameter(i, params[i - 1]);
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

	private List<String> filterIds(Collection<String> rootIds) {
		return rootIds == null ? new ArrayList<>()
				: rootIds.stream().filter(s -> s != null && !s.isEmpty()).map(String::trim)
						.collect(Collectors.toList());

	}

}
