package com.tairan.cloud.credit;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestNFCSReport {
	private String content = null;
	private ParseResult ret = new ParseResult();
	
	@Before
	public void setup() throws URISyntaxException, IOException {
        content = Utils.readFile2String("src/test/resources/nfcs2.json");
    } 
	
	@Test
	public void testParse(){
		Configurations.loadConfigs("src/main/resources/configurations.json");
		JsonParserMulti parser = JsonParserMulti.getInstance();
		ret = parser.parse(content, JsonParser.NFCS_PREFIX);
		System.out.println(ret.getRetCode());
		System.out.println(ret.getErrorCode());
		System.out.println(ret.getErrorMessage());
		System.out.println(ret.getResult());
	}
	
	@After
    public void tear(){

    }
}
