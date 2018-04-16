package fr.insee.rmes.persistance.userRolesManager;

import org.json.JSONArray;

public enum Role {

	ADMIN("Administrateur_RMESGNCS"),
	CONCEPTS_CONTRIBUTOR("Gestionnaire_ensemble_concepts_RMESGNCS"),
	COLLECTIONS_CREATOR("Proprietaire_collection_concepts_RMESGNCS"),
	CONCEPTS_CREATOR("Proprietaire_concept_RMESGNCS"),
	GUEST("GUEST");

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
