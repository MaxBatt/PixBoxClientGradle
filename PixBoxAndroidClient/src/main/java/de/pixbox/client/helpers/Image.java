package de.pixbox.client.helpers;

import com.google.gson.annotations.SerializedName;

/**
 * POJO Class for Image: an Object can be created out of JSON representation
 * 
 * @author Max Batt
 */
public class Image {
	
	@SerializedName("id")
	private String id;

	@SerializedName("url")
	private String url;

	@SerializedName("thumbURL")
	private String thumbURL;
	
	@SerializedName("filename")
	private String filename;
	
	@SerializedName("createdAt")
	private String createdAt;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getThumbURL() {
		return thumbURL;
	}

	public void setThumbURL(String thumbURL) {
		this.thumbURL = thumbURL;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}