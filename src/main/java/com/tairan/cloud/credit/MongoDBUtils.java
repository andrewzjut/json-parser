package com.tairan.cloud.credit;

import java.io.IOException;
import java.util.Arrays;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;

public class MongoDBUtils {
	
	public static String fetchConfigs(String host, int port, String database,
			String collection, String user, String password) throws IOException {
		MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
		MongoClient client = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
		DB db = client.getDB(database);
		DBCollection col = db.getCollection(collection);
		db.setReadPreference(ReadPreference.secondaryPreferred());
		DBObject ret = (DBObject) col.find().next();
		try {
			ret.removeField("_id");
		} catch (Exception e) {
			// do nothing
		}
		return ret.toString();
		
	}
	
	public static void main(String[] args) throws IOException {
		String str = fetchConfigs("dds-bp129a2adc8dd7c42154-pub.mongodb.rds.aliyuncs.com", 3717, "credit_cloud_db", "config_report_parse_rule", "creditcloud", "credit95_27cloud");
		System.out.println(str);
	}
}
