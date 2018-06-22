package com.tairan.cloud.credit.prefix;

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
import java.util.List;
import java.util.Map;

/**
 * Created by hzcgx on 2016/10/28.
 */
public class ItemsUnionPrefixBuilder implements PrefixBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ItemsUnionPrefixBuilder.class);
    private ItemsUnionMeta meta;
    private ThreadLocal<Integer> contactNum = new ThreadLocal<Integer>() {
    	public Integer initialValue() {
    		return 0;
    	}
    };
    
    @Override
    public void initialize(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            meta = mapper.readValue(node.toString(), ItemsUnionMeta.class);
        } catch (IOException e) {
            logger.info(node.toString());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String build(JsonNode node, LevelInfo levelInfo, Map<String, String> errorInfo) {
        String prefix = levelInfo.prefix;
        StringBuilder sb = new StringBuilder();

        for (String item : meta.getUnion()){
            Map<String, String> replace = meta.getReplaces().get(item);
            
            //要提前到前缀中的字段必须存在
            if(null == node.get(item)) {
            	if(!meta.isIfNullNotEmitError()) {
            		errorInfo.put(new String(ErrorDetail.ERROR_CODE_NULL_FILED), "field :" + item + " is null");
            	} else {
            		sb.append(prefix);
				}
            } else {
            	if(sb.length() == 0) {
            		if (meta.isRemoveIndex()) {
                        int end = prefix.lastIndexOf(JsonParser.PREFIX_CONNECTOR);
                        prefix = prefix.substring(0, end);
                    }
            		sb.append(prefix);
            	}
            	
            	String key = node.get(item).asText();
                
                if(meta.getSameFields().contains(key)) {
                	int tmp = contactNum.get() + 1;
                	contactNum.set(tmp );
                	key = key + contactNum.get();
                }
                
                if (replace != null){
                    String replaceStr = replace.get(key);
                    //要被替换的字段不存在，报错
                    if (replaceStr == null){
                    	String e = String.format("could not find key '%s' in repalce map: %s",
                                key, Utils.mapKeySet2String(replace));
                        logger.error(e);
                        errorInfo.put(new String(ErrorDetail.ERROR_CODE_PREFIX_REPLACE), e);
                    }
                    else {
                        key = replaceStr;
                    }
                }
                sb.append(JsonParser.PREFIX_CONNECTOR);
                sb.append(key);
			}
        }
        return sb.toString();
    }

	@Override
	public void emptyTempState() {
		contactNum.set(0);
	}

}

class ItemsUnionMeta{

    private Map<String, Map<String, String>> replaces;
    private List<String> union;
    private List<String> sameFields;
    private boolean removeIndex = true;
    private boolean ifNullNotEmitError = false;

    public Map<String, Map<String, String>> getReplaces() {
        return replaces;
    }
    public void setReplaces(Map<String, Map<String, String>> replaces) {
        this.replaces = replaces;
    }
    public List<String> getUnion() {
        return union;
    }
    public void setUnion(List<String> union) {
        this.union = union;
    }
	public List<String> getSameFields() {
		return sameFields;
	}
	public void setSameFields(List<String> sameFields) {
		this.sameFields = sameFields;
	}
    public boolean isRemoveIndex() {
        return removeIndex;
    }
    public void setRemoveIndex(boolean removeIndex) {
        this.removeIndex = removeIndex;
    }
	public boolean isIfNullNotEmitError() {
		return ifNullNotEmitError;
	}
	public void setIfNullNotEmitError(boolean ifNullNotEmitError) {
		this.ifNullNotEmitError = ifNullNotEmitError;
	}
}
