package fr.insee.rmes.persistance.securityManager;

import fr.insee.rmes.persistance.userRolesManager.Role;

public class User {
	
	Role role;
	String stamp = "";
	
	public User() {
		super();
	}
	
	public User(String role, String stamp) {
		this.role = Role.findRole(role);
		this.stamp = stamp;
	}
	
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public String getStamp() {
		return stamp;
	}
	public void setStamp(String stamp) {
		this.stamp = stamp;
	}
	
}
