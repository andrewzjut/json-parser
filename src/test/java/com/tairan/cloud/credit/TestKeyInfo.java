package com.tairan.cloud.credit;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hzcgx on 2016/10/28.
 */
public class TestKeyInfo {

    @Before
    public void setup(){

        LevelInfo i = new LevelInfo("td.response.report.risk_items.1", 5, false);

        LevelInfo t = new LevelInfo(null, 5, false);
        t.pattern = "td\\.response\\.report\\.risk_items\\.\\d+";

        System.out.println(i.equals(t));

//        Pattern p = Pattern.compile("td\\.response\\.report\\.risk_items.\\d+");
//        Matcher m = p.matcher("td.response.report.risk_items.1");
//        System.out.println(m.matches());
        Set<LevelInfo> s = new HashSet<>();
        s.add(i);
        s.add(t);
    }

    @Test
    public void test(){

    }
}
