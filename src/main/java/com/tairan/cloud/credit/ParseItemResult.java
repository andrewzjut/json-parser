package com.tairan.cloud.credit;

import java.util.List;

/**
 * Created by hzcgx on 2016/11/29.
 */
public class ParseItemResult {

    private int retCode;
    private List<String> result;
    private String errorCode;
    private String errorMessage;

    public ParseItemResult() {
        retCode = 0;
    }

    public int getRetCode() {
        return retCode;
    }
    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }
    public List<String> getResult() {
        return result;
    }
    public void setResult(List<String> result) {
        this.result = result;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
