package com.tairan.cloud.credit.item;

import com.fasterxml.jackson.databind.JsonNode;
import com.tairan.cloud.credit.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by hzcgx on 2016/11/24.
 */
public class CommonItemBuilder implements ItemBuilder{

    private static final Logger logger = LoggerFactory.getLogger(CommonItemBuilder.class);
    private Map<String, String> dictionary = new HashMap<>();
    private Stack<String> symbols = new Stack<>();

    @Override
    public void initialize(JsonNode node) {

        Utils.check(node.has("dictionary"), "could not find 'dictionary' parameter in configuration");
        Iterator<Map.Entry<String, JsonNode>> dictIter = node.get("dictionary").fields();
        while (dictIter.hasNext()){
            Map.Entry<String, JsonNode> entry = dictIter.next();
            dictionary.put(entry.getKey(), entry.getValue().asText());
        }
    }

    @Override
    public List<String> parseItem(JsonNode node) {
        List<String> arrays = new ArrayList<>();

        IndentInfo indentInfo = new IndentInfo(1, true, true);

        symbols.clear();
        parseItem0(arrays, new StringBuilder(), node, indentInfo);

        return arrays;
    }

    private void parseItem0(List<String> arrays, StringBuilder sb, JsonNode node, IndentInfo indent){

        if (node.isArray()){
            sb.append("[");
            symbols.push("]");

            Iterator<JsonNode> elements = node.elements();
            boolean isFirst = true;
            while(elements.hasNext()){
                JsonNode element = elements.next();

                if (element.isArray() || element.isObject()){

                    boolean isLast = false;
                    if (!elements.hasNext()){
                        isLast = true;
                    }
                    IndentInfo elementIndent = new IndentInfo(indent.depth + 1, isFirst, isLast);
                    if (!isFirst){
                        indent(sb, indent.depth);
                    }
                    isFirst = false;

                    parseItem0(arrays, sb, element, elementIndent);
                    if (elementIndent.last){
                        String str = arrays.get(arrays.size() - 1).concat(symbols.pop());
                        arrays.remove(arrays.size() - 1);
                        arrays.add(str);
                    }
                } else if (element.isValueNode()){

                    if (!isFirst){
                        indent(sb, indent.depth);
                    }
                    isFirst = false;

                    sb.append(element.asText());

                    if (!elements.hasNext()){
                        sb.append(symbols.pop());
                    }

                    arrays.add(sb.toString());
                    sb.delete(0, sb.length());
                } else {
                    throw new RuntimeException("ArrayNode's element neither ArrayNode, ObjectNode nor ValueNode");
                }
            }
        } else if (node.isObject()){
            sb.append("{");
            symbols.push("}");
            boolean isFirst = true;

            Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
            while(iter.hasNext()){
                Map.Entry<String, JsonNode> entry = iter.next();
                String key = entry.getKey();
                JsonNode element = entry.getValue();

                if (element.isArray() || element.isObject()){

                    boolean isLast = false;
                    if (!iter.hasNext()){
                        isLast = true;
                    }

                    if (!isFirst){
                        indent(sb, indent.depth);
                    }
                    String chineseKey = dictionary.get(key);
                    if (chineseKey == null){
                        sb.append(key);
                    } else {
                        sb.append(chineseKey);
                    }
                    sb.append(" : ");
                    arrays.add(sb.toString());
                    sb.delete(0, sb.length());

                    IndentInfo elementIndent = new IndentInfo(indent.depth + 2, isFirst, isLast);
                    indent(sb, indent.depth + 1);
                    isFirst = false;

                    parseItem0(arrays, sb, element, elementIndent);
                    if (elementIndent.last){
                        String str = arrays.get(arrays.size() - 1).concat(symbols.pop());
                        arrays.remove(arrays.size() - 1);
                        arrays.add(str);
                    }
                } else if (element.isValueNode()){

                    if (!isFirst){
                        indent(sb, indent.depth);
                    }
                    isFirst = false;

                    String chineseKey = dictionary.get(key);
                    if (chineseKey == null){
                        sb.append(key);
                    } else {
                        sb.append(chineseKey);
                    }
                    sb.append(" : ");
                    sb.append(element.asText());

                    if (!iter.hasNext()){
                        sb.append(symbols.pop());
                    }

                    arrays.add(sb.toString());
                    sb.delete(0, sb.length());
                } else {
                    throw new RuntimeException("ObjectNode's element neither ArrayNode, ObjectNode nor ValueNode");
                }
            }

        } else {
            throw new RuntimeException("JsonNode neither ArrayNode nor ObjectNode");
        }
    }

    private void indent(StringBuilder sb, int depth){
        for (int i = 0; i < depth; i++){
            sb.append(" ");
        }
    }
}
