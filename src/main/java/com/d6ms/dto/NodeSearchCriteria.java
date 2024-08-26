package com.d6ms.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.d6ms.type.NodeType;
import com.d6ms.type.State;
import com.d6ms.utils.Utils;

import lombok.Data;

@Data
public class NodeSearchCriteria {

	private String storeId;

	private Collection<String> ids;

	private Collection<String> businessKeys;

	private Collection<String> masterIds;

	private Collection<String> masterTypes;

	private String name;

	private String parentId;

	private Map<String, String> indexes = new TreeMap<>();

	private List<State> states;

	private NodeType type;

	private boolean loadMetadata;

	private int pageSize;

	private int pageNumber;

	@Override
	public String toString() {
		return Utils.toJson(this, true);
	}

}
