package com.d6ms.dto;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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

	public String print() {

		String s = "[" + type.name().charAt(0) + "] ";
		s += "[" + StringUtils.leftPad("" + level, 3, '0') + "] -";
		s += StringUtils.repeat("--", level - 1) + " " + name;

		if (children != null) {
			for (NodeTreeElement e : children.values()) {
				s += "\n" + e.print();
			}
		}

		return s;

	}

}
