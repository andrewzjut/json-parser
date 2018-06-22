package com.tairan.cloud.credit.item;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Created by hzcgx on 2016/11/24.
 */
public interface ItemBuilder {

    void initialize(JsonNode node);

    List<String> parseItem(JsonNode node);

}
