package com.origin.storage.model;

import java.util.List;

public class FileDataAsJson {

    public List<String> headers;
	
	private String data;
	
	private String userId;

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
	
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
