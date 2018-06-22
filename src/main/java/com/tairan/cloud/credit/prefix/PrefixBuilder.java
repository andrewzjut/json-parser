package com.tairan.cloud.credit.prefix;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.LevelInfo;

/**
 * Created by hzcgx on 2016/10/27.
 */
public interface PrefixBuilder{
	
	void emptyTempState();
	
    void initialize(JsonNode node);

    String build(JsonNode node, LevelInfo levelInfo, Map<String, String> errorInfo);
}
