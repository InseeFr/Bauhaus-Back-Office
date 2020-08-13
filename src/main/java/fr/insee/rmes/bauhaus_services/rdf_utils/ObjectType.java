package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.ontologies.GEO;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.ORG;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;


public enum ObjectType {
	CONCEPT("concept", SKOS.CONCEPT,  Config.CONCEPTS_BASE_URI),
	COLLECTION("collection", SKOS.COLLECTION,  Config.COLLECTIONS_BASE_URI),
	FAMILY("family", INSEE.FAMILY, Config.OP_FAMILIES_BASE_URI),
	SERIES("series", INSEE.SERIES, Config.OP_SERIES_BASE_URI),
	OPERATION("operation", INSEE.OPERATION, Config.OPERATIONS_BASE_URI),
	INDICATOR("indicator", INSEE.INDICATOR, Config.PRODUCTS_BASE_URI),
	DOCUMENTATION("documentation", SDMX_MM.METADATA_REPORT, Config.DOCUMENTATIONS_BASE_URI),
	DOCUMENT("document", FOAF.DOCUMENT, Config.DOCUMENTS_BASE_URI ),
	LINK("link", FOAF.DOCUMENT, Config.LINKS_BASE_URI ),
	GEO_STAT_TERRITORY("geoFeature", GEO.FEATURE, Config.DOCUMENTATIONS_GEO_BASE_URI),
	ORGANIZATION("organization",ORG.ORGANIZATION, ""),
	STRUCTURE("structure", QB.DATA_STRUCTURE_DEFINITION, Config.STRUCTURES_BASE_URI),

	MEASURE_PROPERTY("measureProperty", QB.MEASURE_PROPERTY, Config.STRUCTURES_COMPONENTS_BASE_URI  + "mesure"),
	ATTRIBUTE_PROPERTY("attributeProperty", QB.ATTRIBUTE_PROPERTY, Config.STRUCTURES_COMPONENTS_BASE_URI + "attribut"),
	DIMENSION_PROPERTY("dimensionProperty", QB.DIMENSION_PROPERTY, Config.STRUCTURES_COMPONENTS_BASE_URI + "dimension"),


	UNDEFINED("undefined",null, "");
	
	

	
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
		return Config.BASE_URI_GESTION + this.baseUri;
	}
	
	public String getBaseUriPublication() {
		return Config.BASE_URI_PUBLICATION + this.baseUri;
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
		return Config.BASE_URI_GESTION + baseUri + "/" + id;
	}
}

