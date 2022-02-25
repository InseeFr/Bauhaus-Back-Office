package fr.insee.rmes.config.auth.user;

import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.springframework.security.core.GrantedAuthority;

public class User {
	
	private JSONArray roles;
	private String stamp = "";
	private Collection<GrantedAuthority> authorities;
	
	public User() {
		super();
	}
	
	public User(JSONArray roles, String stamp) {
		this.roles = roles;
		this.stamp = stamp;
	}
	
	public User(List<String> roles, String stamp) {
		this.roles = new JSONArray(roles);
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
	public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }
    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
	
}
