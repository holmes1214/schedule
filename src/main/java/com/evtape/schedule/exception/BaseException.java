package com.evtape.schedule.exception;


import com.evtape.schedule.consts.ResponseMeta;

public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private ResponseMeta errorCode;
	
	public BaseException(String msg) {
		super(msg);
	}
	
	public BaseException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public BaseException(ResponseMeta errorCode) {
		super(errorCode.name());
		this.errorCode = errorCode;
	}
	
	public ResponseMeta getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ResponseMeta errorCode) {
		this.errorCode = errorCode;
	}

}
