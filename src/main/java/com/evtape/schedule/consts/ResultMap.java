package com.evtape.schedule.consts;

public class ResultMap {

	public ResultMap() {
	}
	public ResultMap(ResultCode resultCode) {
		this.status = resultCode.getStatus();
		this.message = resultCode.getMessage();
	}
	private String status;
	private String message;
	private Object data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
