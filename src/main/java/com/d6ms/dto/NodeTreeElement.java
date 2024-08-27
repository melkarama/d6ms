package com.d6ms.dto;

import java.util.Map;

import com.d6ms.type.NodeType;
import com.d6ms.utils.Utils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class NodeTreeElement {

	private String id;

	private String name;

	private String parentId;

	private String storeId;

	private NodeType type;

	private int level;

	private Map<String, NodeTreeElement> children;

	@Override
	public String toString() {
		return Utils.toJson(this, true);
	}

}
