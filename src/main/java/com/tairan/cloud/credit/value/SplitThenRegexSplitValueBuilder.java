package com.tairan.cloud.credit.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.ErrorDetail;
import com.tairan.cloud.credit.LevelInfo;
import com.tairan.cloud.credit.Utils;

public class SplitThenRegexSplitValueBuilder implements ValueBuilder {

	private static final Logger logger = LoggerFactory.getLogger(SplitThenRegexSplitValueBuilder.class);

	private String splitter;
    private String regex;
    private List<String> invalidDataEvidence = new ArrayList<String>();
    private int count;
    private boolean outputSelf;
    private String notMatchOutput;
    private List<String> keyList = new ArrayList<>();
    private Pattern p;
    
	@Override
	public void initialize(JsonNode node) {
		
		splitter = node.get("spliter").asText();
		regex = node.get("regex").asText();
        count = node.get("count").asInt();
        
        outputSelf = false;
        if(null != node.get("outputSelf") && node.get("outputSelf").asText().equals("true")) {
        	outputSelf = true;
        }
        
        if(null != node.get("notMatchOutput")) {
        	notMatchOutput = node.get("notMatchOutput").asText();
        }
        
        if(null != node.get("output")){
        	Iterator<JsonNode> iter = node.get("output").elements();
            while(iter.hasNext()){
                keyList.add(iter.next().asText());
            }
        }
        
        if(null != node.get("invalidDataEvidence")) {
        	Iterator<JsonNode> iter = node.get("invalidDataEvidence").elements();
            while(iter.hasNext()){
            	invalidDataEvidence.add(iter.next().asText());
            }
        }

        p = Pattern.compile(regex);
        Utils.check(keyList.size() == count, "'output' size not equal to 'count'");
	}

	@Override
	public void put(Map<String, Object> result, JsonNode node,
			LevelInfo levelInfo, Map<String, String> errorInfo) {
		String text = node.asText();
		if(outputSelf) {
        	result.put(levelInfo.prefix, text);
        }
		
		if(text == null || text.isEmpty()) {
        	return;
        }
		
		if(null == notMatchOutput) {
        	notMatchOutput = "";
        }
		
		String[] elems = text.split(splitter);
		for(int j = 0; j < elems.length; ++j) {
			String elem = elems[j];
			Matcher m = p.matcher(elem);
	        boolean isMatch = true;
	        
	        try {
	        	Utils.check(m.matches(), String.format("regex '%s' not match: %s", regex, elem));
	            Utils.check(m.groupCount() == count, String.format("count '%s' not match: groupCount '%s'",
	                    count, m.groupCount()));
			} catch (Exception e) {
				boolean isError = true;
				for(String evidence : invalidDataEvidence) {
					Pattern pat = Pattern.compile(evidence);
					Matcher mat = pat.matcher(text);
					if(mat.matches()) {
						isError = false;
						break;
					}
				}
				if(isError) {
					errorInfo.put(new String(ErrorDetail.ERROR_CODE_REGEX_MATCH), "regex: " + regex + " not match: " + elem);
				}
				isMatch = false;
			}
	        
	        for (int i = 0; i < count; i++){
	            String key = Utils.createKey(levelInfo.prefix + "_" + (j + 1), keyList.get(i));
	            Object old = result.put(key, isMatch ? m.group(i + 1) : notMatchOutput);
	            if (old != null) {
	            	String eString = String.format("duplicative key '%s', value is '%s'", key, String.valueOf(old));
	                logger.error(eString);
	                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
	            }
	        }
		}
	}

}
