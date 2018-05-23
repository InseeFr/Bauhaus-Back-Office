package fr.insee.rmes.config.roles;

import org.json.JSONArray;

public enum Role {

	ADMIN(Constants.ADMIN),
	CONCEPTS_CONTRIBUTOR(Constants.CONCEPTS_CONTRIBUTOR),
	COLLECTIONS_CREATOR(Constants.COLLECTIONS_CREATOR),
	CONCEPTS_CREATOR(Constants.CONCEPTS_CREATOR);

	private String role = "";

	private Role(String name) {
		this.role = name;
	}

	public String getRole() {
		return this.role;
	}
	
	public static JSONArray findRole(JSONArray names){
		JSONArray roles = new JSONArray();
		names.forEach(name -> {
			for(Role r : values()){
		        if( r.getRole().equals(name)){
		           roles.put(r);
		        }
		    }
		});
	    return roles;
	}

}
