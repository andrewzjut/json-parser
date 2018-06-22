package com.tairan.cloud.credit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hzcgx on 2016/10/27.
 */
public class LevelInfo {

    public String prefix;

    public int depth;

    public boolean isRoot;

    public String pattern;

    public LevelInfo(String prefix, int depth, boolean isRoot){
        this.prefix = prefix;
        this.depth = depth;
        this.isRoot = isRoot;
    }

    public boolean equals(LevelInfo obj) {

        if (this.pattern != null && obj.prefix != null){
            Pattern p = Pattern.compile(this.pattern);
            Matcher m = p.matcher(obj.prefix);

            if (this.depth == obj.depth && m.matches()){
                return true;
            }
        }

        if (this.prefix != null && obj.pattern != null){
            Pattern p = Pattern.compile(obj.pattern);
            Matcher m = p.matcher(this.prefix);

            if (this.depth == obj.depth && m.matches()){
                return true;
            }
        }

        return super.equals(obj);
    }
}
