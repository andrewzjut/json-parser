package com.tairan.cloud.credit.item;

/**
 * Created by hzcgx on 2016/11/24.
 */
public class IndentInfo {
    public int depth;
    public boolean first;
    public boolean last;

    public IndentInfo(int depth, boolean first, boolean last){
        this.depth = depth;
        this.first = first;
        this.last = last;
    }
}
