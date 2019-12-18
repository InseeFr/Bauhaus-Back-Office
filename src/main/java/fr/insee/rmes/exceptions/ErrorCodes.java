package fr.insee.rmes.exceptions;

public class ErrorCodes {

	/*
	 *  403 UNAUTHORIZEDEXCEPTIONS
	 */

	// CONCEPTS

	public static final int CONCEPT_CREATION_RIGHTS_DENIED = 101;
	public static final int CONCEPT_MODIFICATION_RIGHTS_DENIED = 102;
	public static final int CONCEPT_VALIDATION_RIGHTS_DENIED = 103;
	public static final int CONCEPT_MAILING_RIGHTS_DENIED = 104;

	// deletion
	public static final int CONCEPT_DELETION_SEVERAL_GRAPHS = 111;
	//"The concept "+id+" cannot be deleted because it is used in several graphs."
	public static final int CONCEPT_DELETION_LINKED = 112;
	//"The concept "+id+" cannot be deleted because it is linked to other concepts."

	// INDICATORS
	public static final int INDICATOR_CREATION_RIGHTS_DENIED = 201;
	public static final int INDICATOR_MODIFICATION_RIGHTS_DENIED = 202;
	public static final int INDICATOR_VALIDATION_RIGHTS_DENIED = 203;

	// DOCUMENTS
	public static final int DOCUMENT_CREATION_RIGHTS_DENIED = 301;
	public static final int DOCUMENT_CREATION_EXISTING_FILE = 302;
	public static final int DOCUMENT_MODIFICATION_RIGHTS_DENIED = 303;
	public static final int DOCUMENT_DELETION_LINKED = 304;

	// LINKS
	public static final int LINK_CREATION_RIGHTS_DENIED = 401;
	public static final int LINK_MODIFICATION_RIGHTS_DENIED = 403;

	// FAMILIES
	public static final int FAMILY_CREATION_RIGHTS_DENIED = 501;
	public static final int FAMILY_MODIFICATION_RIGHTS_DENIED = 502;
	public static final int FAMILY_VALIDATION_RIGHTS_DENIED = 503;

	// SERIES
	public static final int SERIES_CREATION_RIGHTS_DENIED = 601;
	public static final int SERIES_MODIFICATION_RIGHTS_DENIED = 602;
	public static final int SERIES_VALIDATION_RIGHTS_DENIED = 603;
	public static final int SERIES_VALIDATION_UNPUBLISHED_FAMILY = 604;

	// OPERATIONS
	public static final int OPERATION_CREATION_RIGHTS_DENIED = 701;
	public static final int OPERATION_MODIFICATION_RIGHTS_DENIED = 702;
	public static final int OPERATION_VALIDATION_RIGHTS_DENIED = 703;
	public static final int OPERATION_VALIDATION_UNPUBLISHED_SERIES = 704;

	// SIMS
	public static final int SIMS_CREATION_RIGHTS_DENIED = 801;
	public static final int SIMS_MODIFICATION_RIGHTS_DENIED = 802;
	public static final int SIMS_VALIDATION_RIGHTS_DENIED = 803;
	public static final int SIMS_VALIDATION_UNPUBLISHED_TARGET = 804;

	// COLLECTIONS

	public static final int COLLECTION_CREATION_RIGHTS_DENIED = 901;
	public static final int COLLECTION_MODIFICATION_RIGHTS_DENIED = 902;
	public static final int COLLECTION_VALIDATION_RIGHTS_DENIED = 903;
	public static final int COLLECTION_MAILING_RIGHTS_DENIED = 904;


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
	
	/*
	 *  406 NOTACCEPTABLEEXCEPTIONS
	 */

	// DOCUMENTS
	public static final int DOCUMENT_EMPTY_NAME = 361;
	public static final int DOCUMENT_FORBIDDEN_CHARATER_NAME = 362;

	// LINKS
	public static final int LINK_EMPTY_URL = 461;
	public static final int LINK_EXISTING_URL = 462;
	
	// SERIES
	public static final int SERIES_OPERATION_OR_SIMS = 663;

	
}
