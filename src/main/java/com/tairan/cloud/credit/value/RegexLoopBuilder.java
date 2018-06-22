package com.tairan.cloud.credit.value;

import java.util.ArrayList;
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

public class RegexLoopBuilder implements ValueBuilder {

	private static final Logger logger = LoggerFactory.getLogger(RegexLoopBuilder.class);
	private String spliter;
	private String regex1;
    private String regex2;
    private List<String> invalidDataEvidence = new ArrayList<String>();
    private int count;
    private boolean outputSelf;
    private String notMatchOutput;
    private List<String> keyList = new ArrayList<>();
    private Pattern p1;
    private Pattern p2;
	
	@Override
	public void initialize(JsonNode node) {
		spliter = node.get("spliter").asText();
		regex1 = node.get("regex1").asText();
		regex2 = node.get("regex2").asText();
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

        p1 = Pattern.compile(regex1);
        p2 = Pattern.compile(regex2);
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
		
		int matchCount = 0;
		while(text.length() > 0) {
			Matcher m = p1.matcher(text);
			try {
				String org = "";
	        	Utils.check(m.matches(), String.format("regex '%s' not match: %s", regex1, text));
	            Utils.check(m.groupCount() == 2, String.format("count '%s' not match: groupCount '%s'",
	                    2, m.groupCount()));

	            text = m.group(2);
	            String[] arrays = text.split(spliter);
	            Matcher m1 = null;
	            
	            if(arrays.length > 0) {
	            	String subText = arrays[0];
	            	m1 = p2.matcher(subText);
		            Utils.check(m1.matches(), String.format("regex '%s' not match: %s", regex2, subText));
		            Utils.check(m1.groupCount() == count-1, String.format("count '%s' not match: groupCount '%s'",
		                    count-1, m.groupCount()));
	            } 
	            
	            matchCount++;
	            
	            for (int i = 0; i < count; i++){
		            String key = Utils.createKey(levelInfo.prefix + "_" + (matchCount), keyList.get(i));
		            Object old = null;
		            if(0 == i) {
		            	old = result.put(key, m.group(1));
		            } else {
						old = result.put(key, m1.group(i));
					}

		            if (old != null) {
		            	String eString = String.format("duplicative key '%s', value is '%s'", key, String.valueOf(old));
		                logger.error(eString);
		                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
		            }
		        }
	            
	            if(text.length() > arrays[0].length()) {
	            	text = text.substring(arrays[0].length() + 1);
	            } else {
	            	text = "";
				}
			} catch (Exception e) {
				boolean isError = true;
				if(matchCount == 0) {
					for(String evidence : invalidDataEvidence) {
						Pattern pat = Pattern.compile(evidence);
						Matcher mat = pat.matcher(text);
						if(mat.matches()) {
							isError = false;
							break;
						}
					}
					for (int i = 0; i < count; i++){
			            String key = Utils.createKey(levelInfo.prefix + "_" + 1, keyList.get(i));
			            Object old = result.put(key, notMatchOutput);
			            if (old != null) {
			            	String eString = String.format("duplicative key '%s', value is '%s'", key, String.valueOf(old));
			                logger.error(eString);
			                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
			            }
			        }
				}
				
				if(isError) {
					errorInfo.put(new String(ErrorDetail.ERROR_CODE_REGEX_MATCH), "regex: " + regex1 + " or " + regex2 + " not match: " + text);
				}
				text = "";
			}
		}
	}

}
