package fr.insee.rmes.config.auth.roles;

public class Roles {
	
	private Roles() {
		    throw new IllegalStateException("Utility class");
	}


	public static final String ADMIN = "Administrateur_RMESGNCS";
	
	/* Concepts */
	public static final String CONCEPTS_CONTRIBUTOR = "Gestionnaire_ensemble_concepts_RMESGNCS";
	public static final String COLLECTION_CREATOR = "Proprietaire_collection_concepts_RMESGNCS";
	public static final String CONCEPT_CREATOR = "Proprietaire_concept_RMESGNCS";
	public static final String CONCEPT_CONTRIBUTOR = "Gestionnaire_concept_RMESGNCS";
	
	/* Operations */
	public static final String SERIES_CONTRIBUTOR = "Gestionnaire_serie_RMESGNCS";
	public static final String CODESLIST_CONTRIBUTOR = "Gestionnaire_liste_codes_RMESGNCS";
	public static final String INDICATOR_CONTRIBUTOR = "Gestionnaire_indicateur_RMESGNCS";
	public static final String DATASET_CONTRIBUTOR = "Gestionnaire_jeu_donnees_RMESGNCS";
	public static final String STRUCTURES_CONTRIBUTOR = "Gestionnaire_structures_RMESGNCS";
	public static final String CNIS = "CNIS_RMESGNCS";

}
