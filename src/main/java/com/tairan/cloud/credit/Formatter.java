package com.tairan.cloud.credit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tairan.cloud.credit.prefix.PrefixBuilder;
import com.tairan.cloud.credit.value.ValueBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hzcgx on 2016/10/28.
 */
public class Formatter {

    private static final Logger logger = LoggerFactory.getLogger(Formatter.class);

    private List<FormatterItem> prefixFormatterList = new ArrayList<>();

    private List<FormatterItem> valueFormatterList = new ArrayList<>();

    private Map<String, PrefixBuilder> prefixBuilderMap = new HashMap<>();

    private Map<String, ValueBuilder> valueBuilderMap = new HashMap<>();

    public Formatter(){}

    public void loadProperties(String fileName) throws Exception{
        try {

            String content = Utils.readFile2String(fileName);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(content);
            init(root);

        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new CreditException(ErrorDetail.ERROR_CODE_BUILDER_INIT_FAIL, e.getMessage());
        }
    }

    public void loadProperties(JsonNode node){
        init(node);
    }

    /**
     * 为node节点格式化前缀
     *
     * <p>
     *     注意： 仅当node节点为ObjectNode时，才会调用次方法。
     *
     * @param node  被格式化的节点
     * @param levelInfo 当前节点的层级信息
     * @return
     */
    public String prefixFormat(JsonNode node, LevelInfo levelInfo, Map<String, String> errorInfo){
        int depth = levelInfo.depth;
        String prefix = levelInfo.prefix;

        for (FormatterItem info : prefixFormatterList) {
            Pattern p = Pattern.compile(info.pattern);
            Matcher m = p.matcher(prefix);
            if (info.depth == depth && m.matches()) {
                PrefixBuilder builder = (PrefixBuilder) prefixBuilderMap.get(info.formatterId);
                if (builder == null) {
                    throw new RuntimeException(String.format("could not find formatter '%s'", info.formatterId));
                }
                return builder.build(node, levelInfo, errorInfo);
            }
        }

        //没有匹配的formatter，则正常输出
        return prefix;
    }

    public void valueFormat(Map<String, Object> result, JsonNode node, LevelInfo levelInfo, Map<String, String> errorInfo){
        int depth = levelInfo.depth;
        String prefix = levelInfo.prefix;

        for (FormatterItem info : valueFormatterList){
            Pattern p = Pattern.compile(info.pattern);
            Matcher m = p.matcher(prefix);
            if (info.depth == depth && m.matches()){
                ValueBuilder builder = (ValueBuilder)valueBuilderMap.get(info.formatterId);
                if (builder == null){
                    throw new RuntimeException(String.format("could not find formatter '%s'", info.formatterId));
                }
                builder.put(result, node, levelInfo, errorInfo);
                return;
            }
        }
        
        //没有匹配的foramtter，则正常输出
        if(!node.isArray()) {
        	Utils.insertValueNode(result, levelInfo.prefix, node);
        }
    }
    
    public void emptyTempState() {
    	for(PrefixBuilder item : prefixBuilderMap.values()) {
    		item.emptyTempState();
    	}
    }
    
    private void init(JsonNode node){

        JsonNode formattersNode = node.get("formatters");
        if(null != formattersNode) {
        	initFormatters(formattersNode);
        }

        JsonNode prefixsNode = node.get("prefixs");
        if(null != prefixsNode) {
        	initFormatterInstances(prefixsNode, true);
        }

        JsonNode valuesNode = node.get("values");
        if(null != valuesNode) {
        	initFormatterInstances(valuesNode, false);
        }
    }

    private void initFormatters(JsonNode node){
        if (!node.isArray()){
            throw new RuntimeException("JsonNode 'formatters' is not ArrayNode");
        }

        Iterator<JsonNode> formatters = node.elements();
        while (formatters.hasNext()) {
            JsonNode f = formatters.next();
            String type = f.get("type").asText();
            if ("PREFIX".equals(type)){
                initFormatters(f, true);
            }
            else if ("VALUE".equals(type)){
                initFormatters(f, false);
            }
            else {
                throw new RuntimeException(String.format("formatter type '%s' not support.", type));
            }
        }
    }

    private void initFormatters(JsonNode node, boolean isPrefix){
        String formatterId = node.get("formatter_id").asText();
        String clazzName = node.get("clazz").asText();

        try {
            Class clazz = Class.forName(clazzName);

            if (isPrefix) {
                PrefixBuilder builder = (PrefixBuilder)clazz.newInstance();
                builder.initialize(node.get("parameters"));
                prefixBuilderMap.put(formatterId, builder);
            }
            else {
                ValueBuilder builder = (ValueBuilder)clazz.newInstance();
                builder.initialize(node.get("parameters"));
                valueBuilderMap.put(formatterId, builder);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void initFormatterInstances(JsonNode node, boolean isPrefix){
        if (!node.isArray()){
            throw new RuntimeException("JsonNode 'prefixs' is not ArrayNode");
        }

        Iterator<JsonNode> prefixs = node.elements();
        while (prefixs.hasNext()) {
            JsonNode f = prefixs.next();

            FormatterItem item = new FormatterItem();
            item.formatterId = f.get("formatter_id").asText();
            item.depth = f.get("depth").asInt();
            item.pattern = f.get("pattern").asText();

            if (isPrefix) {
                prefixFormatterList.add(item);
            }
            else{
                valueFormatterList.add(item);
            }
        }
    }

    private class FormatterItem {
        int depth;
        String pattern;
        String formatterId;
		@Override
		public String toString() {
			return "FormatterItem [depth=" + depth + ", pattern=" + pattern + ", formatterId=" + formatterId + "]";
		}
        
        
   }
    
}
