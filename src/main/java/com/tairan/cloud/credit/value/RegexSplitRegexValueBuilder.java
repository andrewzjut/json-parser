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

public class RegexSplitRegexValueBuilder implements ValueBuilder {

	private static final Logger logger = LoggerFactory.getLogger(RegexSplitRegexValueBuilder.class);
	
	private String regex1;
	private String splitter;
	private String regex2;
    private List<String> invalidDataEvidence = new ArrayList<String>();
    private int count1;
    private int count2;
    private List<String> keyList1 = new ArrayList<>();
    private List<String> keyList2 = new ArrayList<>();
    private String prefix2to1;
    private boolean outputSelf;
    private String notMatchOutput;
    private Pattern p1;
    private Pattern p2;
	
	@Override
	public void initialize(JsonNode node) {
		regex1 = node.get("regex1").asText();
		splitter = node.get("spliter").asText();
		regex2 = node.get("regex2").asText();
        
        if(null != node.get("invalidDataEvidence")) {
        	Iterator<JsonNode> iter = node.get("invalidDataEvidence").elements();
            while(iter.hasNext()){
            	invalidDataEvidence.add(iter.next().asText());
            }
        }
        
        count1 = node.get("count1").asInt();
        if(null != node.get("output1")){
        	Iterator<JsonNode> iter = node.get("output1").elements();
            while(iter.hasNext()){
                keyList1.add(iter.next().asText());
            }
        }
        
        count2 = node.get("count2").asInt();
        if(null != node.get("output2")){
        	Iterator<JsonNode> iter = node.get("output2").elements();
            while(iter.hasNext()){
                keyList2.add(iter.next().asText());
            }
        }
        
        prefix2to1 = node.get("prefix2to1").asText();
        
        outputSelf = false;
        if(null != node.get("outputSelf") && node.get("outputSelf").asText().equals("true")) {
        	outputSelf = true;
        }
        
        if(null != node.get("notMatchOutput")) {
        	notMatchOutput = node.get("notMatchOutput").asText();
        }

        p1 = Pattern.compile(regex1);
        p2 = Pattern.compile(regex2);
//        Utils.check(keyList1.size() == count1, "'output' size not equal to 'count'");
        Utils.check(keyList2.size() == count2, "'output' size not equal to 'count'");
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
		
		Matcher m1 = p1.matcher(text);
        boolean isMatch = true;
        try {
        	Utils.check(m1.matches(), String.format("regex '%s' not match: %s", regex1, text));
            Utils.check(m1.groupCount() == count1, String.format("count '%s' not match: groupCount '%s'",
                    count1, m1.groupCount()));
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
				errorInfo.put(new String(ErrorDetail.ERROR_CODE_REGEX_MATCH), "regex: " + regex1 + " not match: " + text);
			}
			isMatch = false;
		}
        
        for(int i = 0; i < keyList1.size(); ++i) {
        	String key = Utils.createKey(levelInfo.prefix, keyList1.get(i));
            Object old = result.put(key, isMatch ? m1.group(i + 1) : notMatchOutput);
            if (old != null) {
            	String eString = String.format("duplicative key '%s', value is '%s'", key, String.valueOf(old));
                logger.error(eString);
                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
            }
        }
        
		if(isMatch) {
			int subCount = Integer.parseInt(m1.group(1));
			if(subCount > 0) {
				String others = m1.group(count1).charAt(0) == ':' ? m1.group(count1).substring(1) : m1.group(count1);
				int j = 1;
				for(String str : others.split(splitter)) {
					Matcher m2 = p2.matcher(str);
					if(m2.matches()) {
						for(int i = 0; i < count2; ++i) {
				        	String key = Utils.createKey(levelInfo.prefix + "_" + prefix2to1 + "_" + j, keyList2.get(i));
				            Object old = result.put(key, m2.group(i + 1).trim());
				            if (old != null) {
				            	String eString = String.format("duplicative key '%s', value is '%s'", key, String.valueOf(old));
				                logger.error(eString);
				                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
				            }
				        }
					} else {
						errorInfo.put(new String(ErrorDetail.ERROR_CODE_REGEX_MATCH), "regex: " + regex2 + " not match: " + str);
						break;
					}
					++j;
				}
				return;
			}
		}
		for(int i = 0; i < count2; ++i) {
			String key = Utils.createKey(levelInfo.prefix + "_" + prefix2to1 + "_1", keyList2.get(i));
	        Object old = result.put(key, notMatchOutput);
	        if (old != null) {
	            String eString = String.format("duplicative key '%s', value is '%s'", key, String.valueOf(old));
	            logger.error(eString);
	            errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
	        }
		}
	}

}
