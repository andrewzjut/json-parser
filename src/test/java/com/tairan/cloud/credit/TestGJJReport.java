package com.tairan.cloud.credit;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGJJReport {
	private String content = null;
	private ParseResult ret = new ParseResult();
	
	@Before
	public void setup() throws URISyntaxException, IOException {
        content = Utils.readFile2String("src/test/resources/jingshu.txt");
    } 
	
	@Test
	public void testParse() throws CreditException{
		Configurations.loadConfigs("src/main/resources/configurations.json");
		JsonParserMulti parser = JsonParserMulti.getInstance();
		ret = parser.parse(content, "jingshu_gjj");
		System.out.println(ret.getResult());
	}
	
	@After
    public void tear(){

    }
}
