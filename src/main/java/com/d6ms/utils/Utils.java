package com.d6ms.utils;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.d6ms.dto.NodeTreeElement;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Utils {

	public static List<NodeTreeElement> flatten(Collection<NodeTreeElement> nodes) {
		List<NodeTreeElement> results = new ArrayList<>();

		if (nodes != null) {
			for (NodeTreeElement n : nodes) {
				if (n != null) {
					results.add(n);
				}
				if (n.getChildren() != null) {
					results.addAll(flatten(n.getChildren().values()));
				}
			}
		}

		return results;
	}

	public static ObjectMapper createJsonObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	public static String toJson(Object obj) {
		return toJson(obj, true);
	}

	public static String toJson(Object obj, boolean format) {
		StringWriter writer = new StringWriter();
		toJson(obj, format, writer);
		return writer.toString();
	}

	public static void toJson(Object obj, boolean format, Writer writer) {
		ObjectMapper mapper = createJsonObjectMapper();
		ObjectWriter objectWriter = format ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
		try {
			objectWriter.writeValue(writer, obj);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static <E> String join(Collection<E> t, String separator, Function<E, String> toStringF) {

		String s = "";

		for (E o : t) {
			if (s.length() > 0) {
				s += separator;
			}
			s += toStringF.apply(o);
		}

		return s;

	}
}
