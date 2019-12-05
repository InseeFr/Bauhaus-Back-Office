package fr.insee.rmes.config.auth.roles;

public class Constants {

	public static final String ADMIN = "Administrateur_RMESGNCS";
	public static final String USER = "Utilisateur_RMESGNCS";
	
	/* Concepts */
	public static final String CONCEPTS_CONTRIBUTOR = "Gestionnaire_ensemble_concepts_RMESGNCS";
	public static final String COLLECTION_CREATOR = "Proprietaire_collection_concepts_RMESGNCS";
	public static final String CONCEPT_CREATOR = "Proprietaire_concept_RMESGNCS";
	public static final String CONCEPT_CONTRIBUTOR = "Gestionnaire_concept_RMESGNCS";
	
	/* Operations */
	public static final String SERIES_CONTRIBUTOR = "Gestionnaire_serie_RMESGNCS";
	public static final String INDICATOR_CONTRIBUTOR = "Gestionnaire_indicateur_RMESGNCS";
	public static final String CNIS = "CNIS_RMESGNCS";
	
	
	/* Spring */
	public static final String SPRING_PREFIX = "ROLE_";
	public static final String SPRING_ADMIN = SPRING_PREFIX + ADMIN;
	public static final String SPRING_CONCEPTS_CONTRIBUTOR = SPRING_PREFIX + CONCEPTS_CONTRIBUTOR;
	public static final String SPRING_COLLECTION_CREATOR = SPRING_PREFIX + COLLECTION_CREATOR;
	public static final String SPRING_CONCEPT_CREATOR = SPRING_PREFIX + CONCEPT_CREATOR;
	public static final String SPRING_CONCEPT_CONTRIBUTOR = SPRING_PREFIX + CONCEPT_CONTRIBUTOR;
	public static final String SPRING_SERIES_CONTRIBUTOR = SPRING_PREFIX + SERIES_CONTRIBUTOR;
	public static final String SPRING_INDICATOR_CONTRIBUTOR = SPRING_PREFIX + INDICATOR_CONTRIBUTOR;
	public static final String SPRING_CNIS = SPRING_PREFIX + CNIS;
}
