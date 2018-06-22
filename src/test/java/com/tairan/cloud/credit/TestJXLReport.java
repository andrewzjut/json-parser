package com.tairan.cloud.credit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ReadPreference;

public class TestJXLReport {
	private String content = null;
	private ParseResult ret = new ParseResult();
	
	@Before
	public void setup() throws URISyntaxException, IOException {
		content = Utils.readFile2String("src/test/resources/jxl.txt");
//        content = Utils.readFile2String("src/test/resources/jxl-test.txt");
    } 
	
	@Test
	public void testParse() throws Exception{
//		Configurations.loadConfigs("192.168.129.145", 27017,"credit_cloud_db", "config_report_parse_rule","credit","credit95_27cloud")
		Configurations.loadConfigs("src/main/resources/configurations.json");
		JsonParserMulti parser = JsonParserMulti.getInstance();
		ret = parser.parse(content, "jxl");
		System.out.println(ret.getRetCode());
		System.out.println(ret.getErrorCode());
		System.out.println(ret.getErrorMessage());
		System.out.println(ret.getResult());
	}
	
	@After
    public void tear(){

    }
}

