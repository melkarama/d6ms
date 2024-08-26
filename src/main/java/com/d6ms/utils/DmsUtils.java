package com.d6ms.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.d6ms.dto.NodeInfo;

public class DmsUtils {

	public static List<NodeInfo> getRootNodes(Collection<NodeInfo> nodes) {
		List<NodeInfo> results = new ArrayList<>();

		for (NodeInfo n : nodes) {
			if (StringUtils.isBlank(n.getParentId())) {
				results.add(n);
			}
		}

		return results;
	}

}
