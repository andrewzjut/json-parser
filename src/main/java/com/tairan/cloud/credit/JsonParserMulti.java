package com.tairan.cloud.credit;

import java.io.InputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
/**
 * Created by hzcgx on 2016/11/3.
 */
public class JsonParserMulti {

    private static JsonParserMulti parserMulti = null;

    private JsonParserMulti(){}

    public static JsonParserMulti getInstance(){
        if (parserMulti == null){
            synchronized (JsonParserMulti.class){
                if (parserMulti == null){
                    parserMulti = new JsonParserMulti();
//                    InputStream in = JsonParserMulti.class.getClassLoader().getResourceAsStream("configurations.json");
//                    Configurations.loadConfigs(in);
                }
            }
        }

        return parserMulti;
    }

    public ParseResult parse(String content, String keyPrefix){
    	ParseResult result = new ParseResult();
        String topic = Configurations.getTopic(keyPrefix);
        
        try {
        	Map<String, String> errCodeAndMsg = new IdentityHashMap<String, String>();
			String dataRet = Configurations.getParser(keyPrefix).parse(content, keyPrefix, topic, errCodeAndMsg);
			result.setResult(dataRet);
			if(errCodeAndMsg.isEmpty()) {
				result.setRetCode(0);
			} else {
				result.setRetCode(-1);
				StringBuffer codeBuffer = new StringBuffer();
				StringBuffer msgBuffer = new StringBuffer();
				for(Entry<String, String> entry : errCodeAndMsg.entrySet()) {
					codeBuffer.append(entry.getKey() + ",");
					msgBuffer.append(entry.getValue() + ",");
				}
				result.setErrorCode(codeBuffer.toString());
				result.setErrorMessage(msgBuffer.toString());
			}
		} catch (CreditException e) {
			result.setRetCode(-1);
			result.setErrorCode(e.getErrorCode());
			result.setErrorMessage(e.getErrorReason());
		} catch (Exception e1) {
			result.setRetCode(-1);
			result.setErrorCode(ErrorDetail.ERROR_CODE_UNKNOWN);
			result.setErrorMessage(e1.getMessage());
		}
        
        return result;
    }

    public ParseItemResult parseItem(String content, String key){
        String topic = Configurations.getTopic(key);

		ParseItemResult result = new ParseItemResult();

		try {
			List<String> arrays = Configurations.getItemParser(key).parse(content, key, topic);
			result.setResult(arrays);
		} catch (CreditException e){
			result.setRetCode(-1);
			result.setErrorCode(e.getErrorCode());
			result.setErrorMessage(e.getMessage());
		}
        return result;
    }
}
