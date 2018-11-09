package com.origin.storage.model;

import java.util.Date;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@EntityScan
@Document
public class FileMetadata {

	@Id
    public String id;
	
	@Indexed(unique=true)
	private String name;
	
	private String userId;
	
	private Date date;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "FileMetadata [id=" + id + ", name=" + name + ", userId=" + userId + ", date=" + date + "]";
	}
	
	
	
}
