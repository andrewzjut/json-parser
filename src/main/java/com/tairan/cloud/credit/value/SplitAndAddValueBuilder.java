package com.tairan.cloud.credit.value;

import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.ErrorDetail;
import com.tairan.cloud.credit.LevelInfo;
import com.tairan.cloud.credit.Utils;

public class SplitAndAddValueBuilder implements ValueBuilder {

	private static final Logger logger = LoggerFactory.getLogger(SplitAndAddValueBuilder.class);
	
	@Override
	public void initialize(JsonNode node) {
		
	}

	@Override
	public void put(Map<String, Object> result, JsonNode node,
			LevelInfo levelInfo, Map<String, String> errorInfo) {
		String prefix = levelInfo.prefix;
        String text = node.asText();
        
		if(24 == text.length()) {
			for(int i = 24; i > 0 ; --i) {
				result.put(prefix + "_" + (i), text.charAt(i - 1));
			}
		} else {
			errorInfo.put(new String(ErrorDetail.ERROR_CODE_VALUE_SPLITANDADD), "24mthsPaymentDescription field's length doesn't equal to 24");
			logger.error("24mthsPaymentDescription field's length doesn't equal to 24");
		}
	}

}
