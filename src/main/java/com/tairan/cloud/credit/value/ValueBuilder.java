package com.tairan.cloud.credit.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.LevelInfo;

import java.util.Map;

/**
 * Created by hzcgx on 2016/10/29.
 */
public interface ValueBuilder {
	
    void initialize(JsonNode node);

    void put(Map<String, Object> result, JsonNode node, LevelInfo levelInfo, Map<String, String> errorInfo);
}
