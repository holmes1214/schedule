package com.evtape.schedule.consts;

public enum ResultCode {
	SUCCESS("200", "OK"), 
	SERVER_ERROR("201", "server端系统异常。"),;

	private String status;
	private String message;

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

	private ResultCode(String status, String message) {
		this.setStatus(status);
		this.setMessage(message);
	}
}
