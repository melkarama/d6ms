package com.d6ms.dto;

import java.util.Map;

import com.d6ms.type.NodeType;
import com.d6ms.utils.Utils;

import lombok.Data;

@Data
public class NodeInfo {

	private String id;

	private String name;

	private String storeId;

	private String parentId;

	private Map<String, String> indexes;

	private String masterId;

	private String masterType;

	private String businessKey;

	private String contentType;

	private long size;

	private String md5;

	private NodeType type;

	@Override
	public String toString() {
		return Utils.toJson(this, true);
	}

}
