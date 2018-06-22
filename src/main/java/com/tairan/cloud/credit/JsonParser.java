package com.tairan.cloud.credit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trcloud.thrift.client.MesdistributeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by hzcgx on 2016/10/27.
 */
public class JsonParser {

    private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
    public static final String TD_PREFIX = "td";
    public static final String NFCS_PREFIX = "nfcs";
    public static final String PREFIX_CONNECTOR = "_";

    private Formatter formatter;

    private ObjectMapper mapper;

    public JsonParser(String fileName) throws Exception{
        formatter = new Formatter();
        formatter.loadProperties(fileName);
        mapper = new ObjectMapper();
    }

    public JsonParser(JsonNode node){
        formatter = new Formatter();
        formatter.loadProperties(node);
        mapper = new ObjectMapper();
    }

    public String parse(String content, String keyPrefix, String topic, Map<String, String> errorInfo) throws Exception {
        formatter.emptyTempState();
        if (Configurations.isRpcEnable()) {
            Utils.check(topic != null, String.format("could not find topic with '%s'", keyPrefix));
            try {
                MesdistributeService.getInstance().sendMessage(topic, content);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return parse(content, keyPrefix, errorInfo);
    }

    public String parse(String content, String keyPrefix, Map<String, String> errorInfo) throws Exception {

        //保存一级json的键值对，为了保证线程安全，作为局部变量
        Map<String, Object> result = new LinkedHashMap<>();

        //标示层级深度，初始为1级
        int depth = 1;
        
        JsonNode root;
		try {
			root = mapper.readTree(content);
		} catch (IOException e1) {
			String msg = String.format("while read json string: %s, exception: %s", content, e1.getMessage());
            logger.error(msg, e1);
            throw new CreditException(ErrorDetail.ERROR_CODE_READ_INVALID_JSON, msg);
		}
		LevelInfo levelInfo = new LevelInfo(keyPrefix, depth, true);
		
		parse0(result, new NodeEntryWrapper(root, levelInfo), errorInfo);

        String resultStr = "{}";
        try {
            resultStr = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            logger.info("result: " + Utils.map2String(result));
            String msg = String.format("exception across while writing result string: %s", e.getMessage());
            logger.error(msg, e);
            throw new CreditException(ErrorDetail.ERROR_CODE_WRITE_JSON_FAIL, msg);
        }

        return resultStr;
    }

    private void parse0(Map<String, Object> result, NodeEntryWrapper nodeWrapper, Map<String, String> errorInfo) {

        JsonNode jsonNode = nodeWrapper.jsonNode;
        LevelInfo levelInfo = nodeWrapper.levelInfo;

        //传入的JsonNode只可能是ObjectNode或者ArrayNode
        if (jsonNode.isObject()){
            //遍历ObjectNode中所有字段
            Iterator<Map.Entry<String, JsonNode>> fieldsIter = jsonNode.fields();

            //当ObjectNode为"{}"时
            if (!fieldsIter.hasNext()) {
                if (levelInfo.isRoot) {
                    logger.info(String.format("could not find any JsonNode in ObjectNode '%s'.", levelInfo.prefix));
                    return;
                }
                else {
                    Utils.insert(result, levelInfo.prefix, null);
                }
            }

            String currentPrefix = formatter.prefixFormat(jsonNode, levelInfo, errorInfo);

            while (fieldsIter.hasNext()) {
                Map.Entry<String, JsonNode> nodeEntry = fieldsIter.next();
                String field = nodeEntry.getKey();
                JsonNode node = nodeEntry.getValue();
                LevelInfo nodeLevel = new LevelInfo(Utils.createKey(currentPrefix, field), levelInfo.depth + 1, false);

                if (node.isValueNode()) {            //JsonNode为ValueNode，直接读取并保存至result中
//                    String key = Utils.createKey(currentPrefix, field);
//                    Utils.insertValueNode(result, key, node);
                    formatter.valueFormat(result, node, nodeLevel, errorInfo);
                }
                else if (node.isObject() || node.isArray()) {        //JsonNode为ObjectNod或者ArrayNode，继续迭代遍历
                    //LevelInfo level = new LevelInfo(Utils.createKey(currentPrefix, field), levelInfo.depth + 1, false);
                    //迭代遍历
                	if (node.isArray()) {
                    	formatter.valueFormat(result, node, nodeLevel, errorInfo);
    				}
                    parse0(result, new NodeEntryWrapper(node, nodeLevel), errorInfo);
                }
                else {
                    String msg = String.format("JsonNode '%s' is not ValueNode, ObjectNode or ArrayNode", currentPrefix);
                    logger.error(msg);
                }
            }
        }
        else if (jsonNode.isArray()){
            //遍历所有ArrayNode中的元素
            Iterator<JsonNode> arrayNodes = jsonNode.elements();
            formatter.valueFormat(result, jsonNode, levelInfo, errorInfo);
            //当ArrayNode为'[]'时
            if (!arrayNodes.hasNext()){
                if (levelInfo.isRoot) {
                    logger.info(String.format("could not find any JsonNode in ArrayNode '%s'.", levelInfo.prefix));
                    return;
                }
                else {
                    Utils.insert(result, levelInfo.prefix, null);
                }
            }

            int index = 1;
            while (arrayNodes.hasNext()) {
                JsonNode arrayItem = arrayNodes.next();
                String arrayItemPrefix = Utils.createKey(levelInfo.prefix, String.valueOf(index));
                LevelInfo nodeLevel = new LevelInfo(arrayItemPrefix, levelInfo.depth + 1, false);

                if (arrayItem.isValueNode()) {     //当ArrayNode中的元素为ValueNode时，直接读取并保存至result中
//                    Utils.insertValueNode(result, arrayItemPrefix, arrayItem);
                    formatter.valueFormat(result, arrayItem, nodeLevel, errorInfo);
                }
                else if (arrayItem.isObject() || arrayItem.isArray()) { //当ArrayNode中的元素为ObjectNode获取ArrayNode时，继续迭代遍历
                    //数组中的元素作为下一个层级
                    LevelInfo level = new LevelInfo(arrayItemPrefix, levelInfo.depth + 1, false);
                    //迭代遍历
                    if(arrayItem.isArray()) {
                    	formatter.valueFormat(result, arrayItem, level, errorInfo);
                    }
                    parse0(result, new NodeEntryWrapper(arrayItem, level), errorInfo);
                }
                else {
                    String msg = String.format("JsonNode '%s' is not ValueNode, ObjectNode or ArrayNode", arrayItemPrefix);
                    logger.error(msg);
                }
                index++;
            }

        }
        else {
            String msg = String.format("JsonNode '%s' is not ValueNode, ObjectNode or ArrayNode", levelInfo.prefix);
            logger.error(msg);
        }
    }

}
