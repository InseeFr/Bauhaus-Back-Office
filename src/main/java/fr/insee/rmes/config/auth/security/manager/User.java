package fr.insee.rmes.config.auth.security.manager;

import java.util.Collection;

import org.json.JSONArray;
import org.springframework.security.core.GrantedAuthority;

public class User {
	
	JSONArray roles;
	String stamp = "";
	Collection<? extends GrantedAuthority> authorities;
	
	public User() {
		super();
	}
	
	public User(JSONArray roles, String stamp) {
		this.roles = roles;
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
	public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
	
}
