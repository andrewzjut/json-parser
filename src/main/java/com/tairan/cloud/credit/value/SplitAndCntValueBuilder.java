package com.tairan.cloud.credit.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.ErrorDetail;
import com.tairan.cloud.credit.LevelInfo;
import com.tairan.cloud.credit.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hzcgx on 2016/10/29.
 */
public class SplitAndCntValueBuilder implements ValueBuilder{

    private static final Logger logger = LoggerFactory.getLogger(SplitAndCntValueBuilder.class);

    private String splitter;
    private Map<String, String> map = new HashMap<>();
    
    @Override
    public void initialize(JsonNode node) {
        splitter = node.get("splitter").asText();

        Iterator<Map.Entry<String, JsonNode>> iter = node.get("map").fields();
        while (iter.hasNext()){
            Map.Entry<String, JsonNode> entry = iter.next();
            map.put(entry.getKey(), entry.getValue().asText());
        }
    }

    @Override
    public void put(Map<String, Object> result, JsonNode node, LevelInfo levelInfo, Map<String, String> errorInfo) {
        String prefix = levelInfo.prefix;
        result.put(prefix, node.asText());
        String[] items = node.asText().split(splitter);
        Map<String, Integer> countMap = new LinkedHashMap<>();
        for (String item : items){
            String r = map.get(item);
            if (r == null){
            	String e = String.format("could not find key '%s' in replace map: %s",
                        item, Utils.mapKeySet2String(map));
                logger.error(e);
//              errorInfo.put(new String(ErrorDetail.ERROR_CODE_VALUE_SPLITANDCNT), e);
                r = item;
            }

            if (!countMap.containsKey(r)){
                countMap.put(r, 1);
            }
            else{
                int i = countMap.get(r);
                countMap.put(r, i + 1);
            }

        }

        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            String outKey = Utils.createKey(prefix, entry.getKey());
            Object old = result.put(outKey, entry.getValue());
            if (old != null) {
                String eString = String.format("duplicative key '%s', value is '%s'", outKey, String.valueOf(old));
                logger.error(eString);
                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
            }
        }
    }
    
}
