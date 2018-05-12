package com.evtape.schedule.exception;


import com.evtape.schedule.util.ErrorCode;

public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private ErrorCode errorCode;
	
	public BaseException(String msg) {
		super(msg);
	}
	
	public BaseException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public BaseException(ErrorCode errorCode) {
		super(errorCode.memo);
		this.errorCode = errorCode;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

}
