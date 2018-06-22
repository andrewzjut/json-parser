package com.tairan.cloud.credit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonParserUtil {

	public static void main(String[] args) throws IOException {
		File outputFile = new File("./output.txt");
		if(outputFile.exists()) {
			outputFile.delete();
		}
		outputFile.createNewFile();
//		String content = Utils.readFile2String("E:\\work\\json-parser\\src\\test\\resources\\jxl-test.txt");
		String content = Utils.readFile2String(args[1]);
		Configurations.loadConfigs("./configurations.json");
		JsonParserMulti parser = JsonParserMulti.getInstance();
		ParseResult ret = parser.parse(content, args[0]);
		System.out.println(ret.getRetCode());
		System.out.println(ret.getErrorCode());
		System.out.println(ret.getErrorMessage());
		PrintWriter pWriter = new PrintWriter(outputFile);
		pWriter.write(ret.getResult().toCharArray());
		pWriter.flush();
		pWriter.close();
	}

}
