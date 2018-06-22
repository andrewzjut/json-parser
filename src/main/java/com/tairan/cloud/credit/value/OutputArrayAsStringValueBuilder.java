package com.tairan.cloud.credit.value;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.LevelInfo;

public class OutputArrayAsStringValueBuilder implements ValueBuilder {
	
	@Override
	public void initialize(JsonNode node) {
		// TODO do nothing
	}

	@Override
	public void put(Map<String, Object> result, JsonNode node,
			LevelInfo levelInfo, Map<String, String> errorInfo) {
		String prefix = levelInfo.prefix;
		String value = "";
		Iterator<JsonNode> nodes = node.elements();
		while(nodes.hasNext()) {
			value += nodes.next().asText() + ";";
		}
		result.put(prefix, value);
	}

}
