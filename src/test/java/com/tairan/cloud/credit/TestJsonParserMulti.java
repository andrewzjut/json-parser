package com.tairan.cloud.credit;

import org.junit.Before;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by hzcgx on 2016/11/3.
 */
public class TestJsonParserMulti {

    @Before
    public void setup(){

    }

    @org.junit.Test
    public void testParse() throws InterruptedException, UnsupportedEncodingException {

//        InputStream in = JsonParserMulti.class.getClassLoader().getResourceAsStream("configurations.json");
//        Configurations.loadConfigs(in);

        Configurations.loadConfigs("192.168.129.145", 27017,"credit_cloud_db", "config_report_parse_rule","credit","credit95_27cloud");

        String content = Utils.readFile2String("src/test/resources/gjj.txt");

        System.out.println(new String(content.getBytes("utf-8")));
        ParseResult result = JsonParserMulti.getInstance().parse(content, "td");
        System.out.println(result.getResult());
//        result = JsonParserMulti.getInstance().parse(content, "jxl_gjj");
//        System.out.println(result.getResult());
//        Thread.sleep(10000000);
    }
}
