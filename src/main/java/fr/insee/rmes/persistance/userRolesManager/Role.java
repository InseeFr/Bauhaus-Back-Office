package fr.insee.rmes.persistance.userRolesManager;

public enum Role {

	ADMIN("Administrateur_RMESGNCS"),
	GESTIONNAIRE_CONCEPTS("Gestionnaire_ensemble_concepts_RMESGNCS"),
	PROPRIETAIRE_COLLECTION("Proprietaire_collection_concepts_RMESGNCS"),
	PROPRIETAIRE_CONCEPTS("Proprietaire_concept_RMESGNCS");

	private String role = "";

	Role(String name) {
		this.role = name;
	}

	public String getRole() {
		return this.role;
	}

}
