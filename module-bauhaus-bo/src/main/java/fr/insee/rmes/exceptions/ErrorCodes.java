package fr.insee.rmes.exceptions;

public class ErrorCodes {

	/*
	 *  403 UNAUTHORIZEDEXCEPTIONS
	 */

	// CONCEPTS

	public static final int CONCEPT_DELETION_SEVERAL_GRAPHS = 111;
	//"The concept "+id+" cannot be deleted because it is used in several graphs."
	public static final int CONCEPT_DELETION_LINKED = 112;
	//"The concept "+id+" cannot be deleted because it is linked to other concepts."

	// DOCUMENTS
	public static final int DOCUMENT_CREATION_EXISTING_FILE = 302;
	public static final int DOCUMENT_DELETION_LINKED = 304;


	// SERIES
	public static final int SERIES_VALIDATION_UNPUBLISHED_FAMILY = 604;

	// SIMS
	public static final int OPERATION_VALIDATION_UNPUBLISHED_PARENT = 804;

	// COLLECTIONS

	// STRUCTURES

	public static final int COMPONENT_FORBIDDEN_DELETE = 1001;
	public static final int COMPONENT_UNICITY = 1002;
	public static final int STRUCTURE_UNICITY = 1003;
	public static final int COMPONENT_PUBLICATION_EMPTY_CREATOR = 1004;
	public static final int COMPONENT_PUBLICATION_EMPTY_STATUS = 1005;
	public static final int COMPONENT_PUBLICATION_VALIDATED_CONCEPT = 1006;
	public static final int COMPONENT_PUBLICATION_VALIDATED_CODESLIST = 1007;
	public static final int STRUCTURE_PUBLICATION_VALIDATED_COMPONENT = 1008;

	/*
	 *  404 NOTFOUNDEXCEPTIONS
	 */

	// CONCEPTS
	public static final int CONCEPT_UNKNOWN_ID = 141;

	// INDICATORS
	public static final int INDICATOR_UNKNOWN_ID = 241;

	// DOCUMENTS
	public static final int DOCUMENT_UNKNOWN_ID = 341;

	// LINKS
	public static final int LINK_UNKNOWN_ID = 441;

	// FAMILIES
	public static final int FAMILY_UNKNOWN_ID = 541;
	public static final int FAMILY_INCORRECT_BODY = 542;

	// SERIES
	public static final int SERIES_UNKNOWN_ID = 641;
	public static final int SERIES_UNKNOWN_FAMILY = 644;

	// OPERATIONS
	public static final int OPERATION_UNKNOWN_ID = 741;
	public static final int OPERATION_UNKNOWN_SERIES = 744;

	// SIMS
	public static final int SIMS_UNKNOWN_ID = 841;
	public static final int SIMS_UNKNOWN_TARGET = 844;

	//GEOFEATURES
	public static final int GEOFEATURE_UNKNOWN = 845;
	public static final int GEOFEATURE_INCORRECT_BODY = 846;
	public static final int GEOFEATURE_EXISTING_LABEL = 847;

	//CLASSIFICATIONS
	public static final int CLASSIFICATION_UNKNOWN_ID = 1141;
	public static final int CLASSIFICATION_INCORRECT_BODY = 1142;

	//DATASET and DISTRIBUTION
	public static final int DISTRIUBTION_PATCH_INCORRECT_BODY = 1201;
	public static final int DATASET_PATCH_INCORRECT_BODY = 1202;
	public static final int DISTRIBUTION_DELETE_ONLY_UNPUBLISHED = 1203;
	public static final int DATASET_DELETE_ONLY_UNPUBLISHED = 1203 ;
	public static final int DATASET_DELETE_ONLY_WITHOUT_DISTRIBUTION = 1204 ;
	public static final int DATASET_DELETE_ONLY_WITHOUT_DERIVED_DATASET = 1205 ;


	/*
	 *  406 NOTACCEPTABLEEXCEPTIONS
	 */

	// FAMILY
	public static final String OPERATION_FAMILY_EXISTING_PREF_LABEL_LG1 = "406_OPERATION_FAMILY_OPERATION_FAMILY_EXISTING_PREF_LABEL_LG1";
	public static final String OPERATION_FAMILY_EXISTING_PREF_LABEL_LG2 = "406_OPERATION_FAMILY_OPERATION_FAMILY_EXISTING_PREF_LABEL_LG2";

	// DOCUMENTS
	public static final int DOCUMENT_EMPTY_NAME = 361;
	public static final int DOCUMENT_FORBIDDEN_CHARACTER_NAME = 362;
	public static final String OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG1 = "406_OPERATION_DOCUMENT_OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG1";
	public static final String OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG2 = "406_OPERATION_DOCUMENT_OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG2";

	// LINKS
	public static final int LINK_EMPTY_URL = 461;
	public static final int LINK_EXISTING_URL = 462;
	public static final int LINK_BAD_URL = 463;

	// SERIES
	public static final int SERIES_OPERATION_OR_SIMS = 663;
	public static final String OPERATION_SERIES_EXISTING_PREF_LABEL_LG1 = "406_OPERATION_SERIES_OPERATION_SERIES_EXISTING_PREF_LABEL_LG1";
	public static final String OPERATION_SERIES_EXISTING_PREF_LABEL_LG2 = "406_OPERATION_SERIES_OPERATION_SERIES_EXISTING_PREF_LABEL_LG2";

	public static final String OPERATION_OPERATION_EXISTING_PREF_LABEL_LG1 = "406_OPERATION_OPERATION_OPERATION_OPERATION_EXISTING_PREF_LABEL_LG1";
	public static final String OPERATION_OPERATION_EXISTING_PREF_LABEL_LG2 = "406_OPERATION_OPERATION_OPERATION_OPERATION_EXISTING_PREF_LABEL_LG2";

	// SIMS
	public static final int SIMS_INCORRECT = 861;
	public static final int SIMS_DELETION_FOR_NON_SERIES = 862;
	public static final int SIMS_EXPORT_WITHOUT_LANGUAGE = 863;

	private ErrorCodes() {
		    throw new IllegalStateException("Utility class");
	}
}
