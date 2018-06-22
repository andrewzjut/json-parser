package com.tairan.cloud.credit.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tairan.cloud.credit.ErrorDetail;
import com.tairan.cloud.credit.JsonParser;
import com.tairan.cloud.credit.LevelInfo;
import com.tairan.cloud.credit.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hzcgx on 2016/10/29.
 */
public class RegexAndReplaceValueBuilder implements ValueBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RegexAndReplaceValueBuilder.class);

    private String regex;
    private int count;
    private Map<String, String> map = new HashMap<String, String>();
    private Pattern p;

    @Override
    public void initialize(JsonNode node) {

        regex = node.get("regex").asText();
        count = node.get("count").asInt();

        Iterator<Map.Entry<String, JsonNode>> iter = node.get("map").fields();
        while(iter.hasNext()){
            Map.Entry<String, JsonNode> entry = iter.next();
            map.put(entry.getKey(), entry.getValue().asText());
        }

        p = Pattern.compile(regex);
    }

    @Override
    public void put(Map<String, Object> result, JsonNode node, LevelInfo levelInfo, Map<String, String> errorInfo) {
        String prefix = levelInfo.prefix;
        //TODO 待确认需要加M标示数组索引吗？
        int end = prefix.lastIndexOf(JsonParser.PREFIX_CONNECTOR);
        prefix = prefix.substring(0, end);

        String text = node.asText();
        Matcher m = p.matcher(text);
        try {
        	Utils.check(m.matches(), String.format("regex '%s' not match: %s", regex, text));
            Utils.check(m.groupCount() == count, String.format("count '%s' not match: groupCount '%s'",
                    count, m.groupCount()));
            String key = m.group(1);
            int value = Integer.valueOf(m.group(2));

            String replace = map.get(key);

            if (replace == null){
                logger.error(String.format("could not find key '%s' in replace map: %s'",
                        key, Utils.mapKeySet2String(map)));
                replace = key;
            }

            String outKey = Utils.createKey(prefix, replace);
            Object old = result.put(outKey, value);
            if (old != null) {
            	String eString = String.format("duplicative key '%s', value is '%s'", outKey, String.valueOf(old));
            	logger.error(eString);
                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
            }
		} catch (Exception e) {
			errorInfo.put(new String(ErrorDetail.ERROR_CODE_REGEX_MATCH), "regex: " + regex + " not match: " + text);
		}
    }

}
