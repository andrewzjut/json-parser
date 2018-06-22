package com.tairan.cloud.credit;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hzcgx on 2016/10/26.
 */
public class Test {

    static class ParaMeta{

        private String regex;
        private int count;
        private List<String> output;

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<String> getOutput() {
            return output;
        }

        public void setOutput(List<String> output) {
            this.output = output;
        }

    }

    public static void main(String[] args) throws IOException {

        String regex = "[\\(\\[]([^,]+),([^\\)\\]]+)[\\)\\]]";

        String a = "[100,+)";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(a);
        System.out.println(m.matches());
        System.out.println(m.group(1));
        System.out.println(m.group(2));


//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            ParaMeta meta = mapper.readValue(Utils.readFile2String("src/test/resources/test.json"), ParaMeta.class);
//            String regex = meta.getRegex();
//            int count = meta.getCount();
//        } catch (IOException e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }

    }
}
