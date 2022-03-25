package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.ontologies.GEO;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.ORG;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;


public enum ObjectType {
	CONCEPT(Constants.CONCEPT, SKOS.CONCEPT,  Config.getConceptsBaseUri()),
	COLLECTION(Constants.COLLECTION, SKOS.COLLECTION,  Config.getCollectionsBaseUri()),
	FAMILY(Constants.FAMILY, INSEE.FAMILY, Config.getOpFamiliesBaseUri()),
	SERIES("series", INSEE.SERIES, Config.getOpSeriesBaseUri()),
	OPERATION("operation", INSEE.OPERATION, Config.getOperationsBaseUri()),
	INDICATOR("indicator", INSEE.INDICATOR, Config.getProductsBaseUri()),
	DOCUMENTATION("documentation", SDMX_MM.METADATA_REPORT, Config.getDocumentationsBaseUri()),
	DOCUMENT(Constants.DOCUMENT, FOAF.DOCUMENT, Config.getDocumentsBaseUri() ),
	LINK("link", FOAF.DOCUMENT, Config.getLinksBaseUri() ),
	GEO_STAT_TERRITORY("geoFeature", GEO.FEATURE, Config.getDocumentationsGeoBaseUri()),
	ORGANIZATION("organization",ORG.ORGANIZATION, ""),
	STRUCTURE("structure", QB.DATA_STRUCTURE_DEFINITION, Config.getStructuresBaseUri()),
	CODE_LIST(Constants.CODELIST, QB.CODE_LIST, Config.getCodeListBaseUri()),

	MEASURE_PROPERTY("measureProperty", QB.MEASURE_PROPERTY, Config.getStructuresComponentsBaseUri()  + "mesure"),
	ATTRIBUTE_PROPERTY("attributeProperty", QB.ATTRIBUTE_PROPERTY, Config.getStructuresComponentsBaseUri() + "attribut"),
	DIMENSION_PROPERTY("dimensionProperty", QB.DIMENSION_PROPERTY, Config.getStructuresComponentsBaseUri() + "dimension"),


	UNDEFINED(Constants.UNDEFINED,null, "");
	
	

	
	private String labelType;
	private IRI uri;
	private String baseUri;

	ObjectType(String labelType, IRI uri, String baseUri){
		this.labelType=labelType;
		this.uri=uri;
		this.baseUri=baseUri;
	}

	public IRI getUri() {
		return this.uri;
	}
	
	public String getLabelType() {
		return this.labelType;
	}
	
	public String getBaseUri() {
		return Config.getBaseUriGestion() + this.baseUri;
	}
	
	public String getBaseUriPublication() {
		return Config.getBaseUriPublication() + this.baseUri;
	}
	
	
	private static Map<String, ObjectType> lookupLabel = new HashMap<>();
	private static Map<IRI, ObjectType> lookupUri = new HashMap<>();

	static {
		// Populate out lookup when enum is created
		for (ObjectType e : ObjectType.values()) {
			lookupLabel.put(e.getLabelType(), e);
			lookupUri.put(e.getUri(), e);
		}
	}
	
	/**
	 * Get Enum type by label
	 * @param label
	 * @return
	 */
	public static ObjectType getEnum(String labelType) {
		return lookupLabel.get(labelType)!=null ? lookupLabel.get(labelType): UNDEFINED;
	}
	
	/**
	 * Get URI by label
	 * @param label
	 * @return
	 */
	public static IRI getUri(String labelType) {
		return getEnum(labelType).uri;
	}
	
	/**
	 * Get URI by label
	 * @param label
	 * @return
	 */
	public static String getBaseUri(String labelType) {
		return getEnum(labelType).baseUri;
	}

	/**
	 * Get Enum type by URI
	 * @param labelType
	 * @return
	 */
	public static ObjectType getEnum(IRI uri) {
		return lookupUri.get(uri)!=null ? lookupUri.get(uri): UNDEFINED;
	}
	
	/**
	 * Get label by URI
	 * @param labelType
	 * @return
	 */
	public static String getLabelType(IRI uri) {
		return getEnum(uri).labelType;
	}

	
	/**
	 * Get label by URI
	 * @param label
	 * @return
	 */
	public static String getCompleteUriGestion(String labelType, String id) {
		String baseUri = getBaseUri(labelType);
		return Config.getBaseUriGestion() + baseUri + "/" + id;
	}
}

