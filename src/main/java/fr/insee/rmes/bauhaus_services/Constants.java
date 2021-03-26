package fr.insee.rmes.bauhaus_services;

public class Constants {

	/*A*/
	public static final String ACCRUAL_PERIODICITY_LIST ="accrualPeriodicityList";
	public static final String ALT_LABEL_LG1 = "altLabelLg1";
	public static final String ALT_LABEL_LG2 = "altLabelLg2";

	/*C*/
	public static final String CODELIST = "codeList";
	public static final String CODE_LIST_FREQ = "CL_FREQ";
	public static final String CODE_LIST_SOURCE_CATEGORY = "CL_SOURCE_CATEGORY";
	public static final String CONCEPT = "concept";
	public static final String CREATOR = "creator";
	public static final String CREATORS = "creators";
	public static final String CONTRIBUTOR = "contributor";
	public static final String CONTRIBUTORS = "contributors";


	/*D*/
	public static final String DATA_COLLECTOR = "dataCollector";
	public static final String DATA_COLLECTORS = "dataCollectors";
	public static final String DESCRIPTION_LG1 = "descriptionLg1";
	public static final String DESCRIPTION_LG2 = "descriptionLg2";
	public static final String DOCUMENT = "document";
	public static final String DOCUMENTS_LG1 = "documentsLg1";
	public static final String DOCUMENTS_LG2 = "documentsLg2";
	public static final String DOT_XML = ".xml";
	
	/*F*/
	public static final String FAMILY = "family";
	public static final String FLAT_ODT = "flatODT";
	
	/*G*/
	public static final String GOAL_COMITE_LABEL = "goalLabel";
	public static final String GOAL_RMES = "goalRmes";

	
	/*H*/
	public static final String HAS_DOC_LG1 = "hasDocLg1";
	public static final String HAS_DOC_LG2 = "hasDocLg2";

	
	/*I*/
	public static final String ID = "id";
	public static final String COMPONENT_TYPE = "componentType";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String ID_INDICATOR = "idIndicator";
	public static final String ID_OPERATION = "idOperation";
	public static final String ID_SERIES = "idSeries";
	public static final String ID_SIMS = "idSims";
	public static final String INDICATOR_UP = "INDICATOR";
	public static final String ISREPLACEDBY = "isReplacedBy";
	
	/*L*/
	public static final String LABEL = "label";
	public static final String LABEL_LG1 = "labelLg1";
	public static final String LABEL_LG2 = "labelLg2";
	
	/*M*/
	public static final String MANAGER = "manager";
	
	/*O*/
	public static final String OPERATIONS = "operations";
	public static final String OPERATION_UP = "OPERATION";
	public static final String ORGANIZATIONS = "organizations";
	public static final String OWNER = "owner";
	public static final String OUTPUT = "output";
	
	/*P*/
	public static final String PREF_LABEL_LG = "prefLabelLg";
	public static final String PREF_LABEL_LG1 = "prefLabelLg1";
	public static final String PREF_LABEL_LG2 = "prefLabelLg2";
	public static final String PUBLISHER = "publisher";
	public static final String PUBLISHERS = "publishers";


	/*R*/
	public static final String RANGE_TYPE = "rangeType";
	public static final String REPOSITORY_EXCEPTION = "RepositoryException";
	public static final String REPLACES = "replaces";
	
	/*S*/
	public static final String SERIES_UP = "SERIES";
	public static final String SERIES = "SERIES";
	public static final String SEEALSO = "seeAlso";
	public static final String STAMP = "stamp";
	
	/*T*/
	public static final String TEXT_LG1 = "texte";
	public static final String TEXT_LG2 = "text";
	public static final String TYPE_OF_OBJECT = "typeOfObject";
	public static final String TYPELIST = "typeList";

	/*U*/
	public static final String UNDEFINED = "undefined";
	public static final String UPDATED_DATE = "updatedDate";
	public static final String URI = "uri";
	public static final String URL = "url";

	/*V*/
	public static final String VALUE = "value";

	/*W*/
	public static final String WASGENERATEDBY = "wasGeneratedBy";
	
	/*X*/
	public static final String XML = "xml";
	public static final String XML_EMPTY_TAG = "<empty/>";
	public static final String XML_START_DOCUMENT = "<?xml version=\"1.0\"?>";
	public static final String XML_OPEN_CODELIST_TAG = "<codelist>";
	public static final String XML_END_CODELIST_TAG = "</codelist>";
	public static final String XML_OPEN_PARAMETERS_TAG = "<parameters>";
	public static final String XML_END_PARAMETERS_TAG = "</parameters>";
	public static final String XML_OPEN_LANGUAGES_TAG = "<languages>";
	public static final String XML_END_LANGUAGES_TAG = "</languages>";
	public static final String XML_OPEN_LANGUAGE_TAG = "<language>";
	public static final String XML_END_LANGUAGE_TAG = "</language>";
	public static final String XML_OPEN_TARGET_TYPE_TAG = "<targetType>";
	public static final String XML_END_TARGET_TYPE_TAG = "</targetType>";
	public static final String XML_OPEN_INCLUDE_EMPTY_MAS_TAG = "<includeEmptyMas>";
	public static final String XML_END_INCLUDE_EMPTY_MAS_TAG = "</includeEmptyMas>";
	public static final String XML_INF_REPLACEMENT = "replacementForInf";
	public static final String XML_SUP_REPLACEMENT = "replacementForSup";
	public static final String XML_ESPERLUETTE_REPLACEMENT = "replacementForEsperluette";
	
	private Constants() {
		throw new IllegalStateException("Utility class");
	}
}
