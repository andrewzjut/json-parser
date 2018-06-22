package com.tairan.cloud.credit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trcloud.thrift.util.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hzcgx on 2016/11/3.
 */
public class Configurations {

    private static final Logger logger = LoggerFactory.getLogger(Configurations.class);

    private static Map<String, String> topicMap;
    private static Map<String, JsonParser> parserMap;
    private static Map<String, JsonItemParser> itemParserMap;
    private static boolean rpcEnable = false;

    public synchronized static void loadConfigs(String path){

        String content = Utils.readFile2String(path);
        init(content);
    }

    public synchronized  static void loadConfigs(InputStream in){
        String content = Utils.readStream2String(in);
        init(content);
    }
    
    public synchronized static void loadConfigs(String host, int port, String database,
			String collection, String user, String password) throws CreditException {
    	try {
			String jsonString = MongoDBUtils.fetchConfigs(host, port, database, collection, user, password);
			loadConfigs(new ByteArrayInputStream(jsonString.getBytes("UTF-8")));
    	} catch (IOException e) {
    		logger.error(e.getMessage(), e);
            throw new CreditException(ErrorDetail.ERROR_CODE_INITIALIZE, e.getMessage());
		}	
    }

    private static void init(String content){

        topicMap = new HashMap<>();
        parserMap = new HashMap<>();
        itemParserMap = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(content);

            Iterator<Map.Entry<String, JsonNode>> iter = root.fields();
            while (iter.hasNext()){
                Map.Entry<String, JsonNode> entry = iter.next();
                String key = entry.getKey();
                JsonNode node = entry.getValue();
                if (key.equals("rpc")){
                    //kafka-rpc-client配置初始化
                    Map<String, String> rpcMap = Utils.jsonNode2Map(root.get("rpc"));
                    if (Boolean.valueOf(rpcMap.get("enable"))){
                        rpcEnable = true;
                        Config.setConfig(rpcMap);
                    }
                } else {
                    initParser(key, node);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void initParser(String key, JsonNode node){
        topicMap.put(key, node.get("topic").asText());

        if (node.has("is_item")){
            itemParserMap.put(key, new JsonItemParser(node));
            return;
        }
        parserMap.put(key, new JsonParser(node));
    }

    public static String getTopic(String key){
        Utils.check(topicMap != null, "topicMap is null");
        return topicMap.get(key);
    }

    public static JsonParser getParser(String key){
        Utils.check(parserMap != null, "parserMap is null");
        JsonParser parser = parserMap.get(key);
        Utils.check(parser != null, String.format("could not find JsonParser with '%s'", key));
        return parser;
    }

    public static JsonItemParser getItemParser(String key){
        Utils.check(itemParserMap != null, "itemParserMap is null");
        JsonItemParser parser = itemParserMap.get(key);
        Utils.check(parser != null, String.format("could not find JsonItemParser with '%s'", key));
        return parser;
    }

    public static boolean isRpcEnable() {
        return rpcEnable;
    }
}
