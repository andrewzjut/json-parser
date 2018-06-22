package com.tairan.cloud.credit;

import org.junit.*;

import java.io.InputStream;
import java.util.List;

/**
 * Created by hzcgx on 2016/11/24.
 */
public class TestCommonItemBuilder {

    @Before
    public void setup(){

    }

    @org.junit.Test
    public void test(){

        String content = Utils.readFile2String("src/test/resources/item_common.json");

        InputStream in = JsonParserMulti.class.getClassLoader().getResourceAsStream("configurations.json");
        Configurations.loadConfigs(in);

        ParseItemResult result = JsonParserMulti.getInstance().parseItem(content, "common");

        for (String s : result.getResult()){
            System.out.println(s);
        }

    }
}
