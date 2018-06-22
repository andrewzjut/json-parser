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

public class RegexSplit2ValueBuilder implements ValueBuilder {

	private static final Logger logger = LoggerFactory.getLogger(RegexSplit2ValueBuilder.class);
	
	private String regex1;
	private String regex2;
    private List<String> invalidDataEvidence = new ArrayList<String>();
    private int count;
    private boolean outputSelf;
    private String notMatchOutput;
    private List<String> keyList = new ArrayList<String>();
    private Pattern p1;
    private Pattern p2;
	
	@Override
	public void initialize(JsonNode node) {
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
        
        Matcher m = p1.matcher(text);
        boolean isMatch = true;
        
        if(null == notMatchOutput) {
        	notMatchOutput = "";
        }
        
        try {
        	Utils.check(m.matches(), String.format("regex '%s' not match: %s", regex1, text));
            Utils.check(m.groupCount() == count, String.format("count '%s' not match: groupCount '%s'",
                    count, m.groupCount()));
		} catch (Exception e) {
			isMatch = false;
			
			Matcher m2 = p2.matcher(text);
			if(!m2.matches()) {
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
					errorInfo.put(new String(ErrorDetail.ERROR_CODE_REGEX_MATCH), "regex: " + regex1 + " or " + regex2 + " not match: " + text);
				}
			} else {
				notMatchOutput = "0";
			}
		}
        
        for (int i = 0; i < count; i++){
            String key = Utils.createKey(levelInfo.prefix, keyList.get(i));
            Object old = result.put(key, isMatch ? m.group(i + 1) : notMatchOutput);
            if (old != null) {
            	String eString = String.format("duplicative key '%s', value is '%s'", key, String.valueOf(old));
            	logger.error(eString);
                errorInfo.put(new String(ErrorDetail.ERROR_CODE_KEY_DUPLICATE), eString);
            }
        }
	}

}
