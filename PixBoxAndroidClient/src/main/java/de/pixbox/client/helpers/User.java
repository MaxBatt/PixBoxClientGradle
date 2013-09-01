package de.pixbox.client.helpers;

import com.google.gson.Gson;


/**
 * POJO Class for User: an object of the class can be created out of JSON representation
 * 
 * @author Max Batt
 */
public class User {
	private String id;
	private String username;
	

	// Getter
	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}
	
	public String getJson() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}
}