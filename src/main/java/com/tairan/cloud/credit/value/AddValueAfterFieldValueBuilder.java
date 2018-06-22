package com.tairan.cloud.credit.value;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.common.protocol.Errors;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.JsonParser;
import com.tairan.cloud.credit.LevelInfo;
import com.tairan.cloud.credit.Utils;

public class AddValueAfterFieldValueBuilder implements ValueBuilder {

	private Map<String, String> map = new HashMap<String, String>();
	private String keyPrefix = "";
	
	@Override
	public void initialize(JsonNode node) {

        Iterator<Map.Entry<String, JsonNode>> iter = node.get("map").fields();
        while(iter.hasNext()){
            Map.Entry<String, JsonNode> entry = iter.next();
            map.put(entry.getKey(), entry.getValue().asText());
        }
        keyPrefix = node.get("keyPrefix").asText();
	}

	@Override
	public void put(Map<String, Object> result, JsonNode node,
			LevelInfo levelInfo, Map<String, String> errorInfo) {
		String prefix = levelInfo.prefix;
        String text = node.asText();
        String outKey = prefix + keyPrefix;
        result.put(outKey, text);
        
        String outValue = map.get(text);
        if(null == outValue || outValue.isEmpty()) {
        	outValue = "Cnt_" + text;
        }
        outValue = prefix + "_" + outValue;
        result.put(outValue, "1");
	}

}
