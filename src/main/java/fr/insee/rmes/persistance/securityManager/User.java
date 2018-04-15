package fr.insee.rmes.persistance.securityManager;

import org.json.JSONArray;

import fr.insee.rmes.persistance.userRolesManager.Role;

public class User {
	
	JSONArray roles;
	String stamp = "";
	
	public User() {
		super();
	}
	
	public User(JSONArray roles, String stamp) {
		this.roles = Role.findRole(roles);
		this.stamp = stamp;
	}
	
	public JSONArray getRoles() {
		return roles;
	}
	public void setRoles(JSONArray roles) {
		this.roles = roles;
	}
	public String getStamp() {
		return stamp;
	}
	public void setStamp(String stamp) {
		this.stamp = stamp;
	}
	
}
