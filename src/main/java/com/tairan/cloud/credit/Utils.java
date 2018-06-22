package com.tairan.cloud.credit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hzcgx on 2016/10/27.
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String readFile2String(String fileName){

        try {
            return readStream2String(new FileInputStream(fileName));
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String readStream2String(InputStream in){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            StringBuilder sb = new StringBuilder();
            String s = null;
            while ((s = reader.readLine()) != null) {
                sb.append(s);
            }

            return sb.toString();
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public static String mapKeySet2String(Map<String, String> map){
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        sb.append("[");
        for (String s : map.keySet()){
            if (!first) {
                sb.append(",");
            }
            sb.append(s);
            first = false;
        }

        sb.append("]");
        return sb.toString();
    }

    public static String mapValueSet2String(Map<String, String> map){
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        sb.append("[");
        for (String s : map.values()){
            if (!first) {
                sb.append(",");
            }
            sb.append(s);
            first = false;
        }

        sb.append("]");
        return sb.toString();
    }

    public static String map2String(Map<String, Object> map){

        StringBuilder sb = new StringBuilder();

        for (String s : map.keySet()){
            sb.append(s);
            sb.append(":");
            sb.append(String.valueOf(map.get(s)));
            sb.append(",");
        }

        return sb.substring(0, sb.length() - 2);
    }

    public static void insertValueNode(Map<String, Object> result, String key, JsonNode valueNode) {
        if (valueNode.isNull()) {
            insert(result, key, null);
        } else if (valueNode.isInt()) {
            insert(result, key, valueNode.intValue());
        } else if (valueNode.isFloat()) {
            insert(result, key, valueNode.floatValue());
        } else if (valueNode.isDouble()) {
            insert(result, key, valueNode.doubleValue());
        } else if (valueNode.isBoolean()) {
            insert(result, key, valueNode.booleanValue());
        } else if (valueNode.isShort()) {
            insert(result, key, valueNode.shortValue());
        } else if (valueNode.isLong()) {
            insert(result, key, valueNode.longValue());
        } else if (valueNode.isTextual()) {
            insert(result, key, valueNode.textValue());
        } else {
            String msg = String.format("Could not found type for key '%s', value '%s', deal as TextValue",
                    key, valueNode.asText());
            logger.info(msg);
            insert(result, key, valueNode.asText());
        }
    }

    public static void insert(Map<String, Object> result, String key, Object value) {
        Object old = result.put(key, value);
        if (old != null) {
            String msg = String.format("duplicative key '%s', old value '%s', new value '%s'",
                    key, String.valueOf(old), String.valueOf(value));
            logger.warn(msg);
        }
    }

    public static String createKey(String prefix, String itemKey){
        return prefix + JsonParser.PREFIX_CONNECTOR + itemKey;
    }

    public static Map<String, String> jsonNode2Map(JsonNode node){
        Map<String, String> map = new HashMap<>();
        if (!node.isObject()){
            logger.error("jsonNode2Map only support ObjectNode");
            return map;
        }

        Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
        while(iter.hasNext()){
            Map.Entry<String, JsonNode> entry = iter.next();
            if (!entry.getValue().isValueNode()){
                logger.error("jsonNode2Map only support ValueNode in ObjectNode");
                continue;
            }
            map.put(entry.getKey(), entry.getValue().asText());
        }

        return map;
    }

    public static void check(boolean success, String msg){
        if (!success) {
            throw new RuntimeException(msg);
        }
    }
}
