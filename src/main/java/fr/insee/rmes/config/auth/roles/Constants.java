package fr.insee.rmes.config.auth.roles;

public class Constants {

	public static final String ADMIN = "Administrateur_RMESGNCS";
	public static final String CONCEPTS_CONTRIBUTOR = "Gestionnaire_ensemble_concepts_RMESGNCS";
	public static final String COLLECTIONS_CREATOR = "Proprietaire_collection_concepts_RMESGNCS";
	public static final String CONCEPTS_CREATOR = "Proprietaire_concept_RMESGNCS";
	
	public static final String SPRING_PREFIX = "ROLE_";
	public static final String SPRING_ADMIN = SPRING_PREFIX + ADMIN;
	public static final String SPRING_CONCEPTS_CONTRIBUTOR = SPRING_PREFIX + CONCEPTS_CONTRIBUTOR;
	public static final String SPRING_COLLECTIONS_CREATOR = SPRING_PREFIX + COLLECTIONS_CREATOR;
	public static final String SPRING_CONCEPTS_CREATOR = SPRING_PREFIX + CONCEPTS_CREATOR;
}
