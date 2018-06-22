package com.tairan.cloud.credit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tairan.cloud.credit.item.ItemBuilder;
import com.trcloud.thrift.client.MesdistributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by hzcgx on 2016/11/24.
 */
public class JsonItemParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonItemParser.class);
    private ItemBuilder builder;
    private ObjectMapper mapper;

    public JsonItemParser(JsonNode node){

        String clazzName = node.get("clazz").asText();
        try {
            Class clazz = Class.forName(clazzName);
            builder = (ItemBuilder)clazz.newInstance();
            builder.initialize(node.get("parameters"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CreditException(ErrorDetail.ERROR_CODE_BUILDER_INIT_FAIL, e.getMessage());
        }

        mapper = new ObjectMapper();
    }

    public List<String> parse(String content, String key, String topic){
        if (Configurations.isRpcEnable()) {
            Utils.check(topic != null, String.format("could not find topic with '%s'", key));
            try {
                MesdistributeService.getInstance().sendMessage(topic, content);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            JsonNode node = mapper.readTree(content);
            return builder.parseItem(node);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new CreditException(ErrorDetail.ERROR_CODE_ITEM_PARSER_FAIL, e.getMessage());
        }
    }
}
