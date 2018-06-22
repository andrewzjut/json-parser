package com.tairan.cloud.credit;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by hzcgx on 2016/10/28.
 */
public class NodeEntryWrapper {
    public JsonNode jsonNode;

    public LevelInfo levelInfo;

    public NodeEntryWrapper(JsonNode node, LevelInfo levelInfo){
        this.jsonNode = node;
        this.levelInfo = levelInfo;
    }
}
