package com.tairan.cloud.credit;

public class CreditException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1338229443478865453L;
	private String errorCode;
	private String errorReason;
	
	public CreditException(String code, String reason) {
		this.setErrorCode(code);
		this.setErrorReason(reason);
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
