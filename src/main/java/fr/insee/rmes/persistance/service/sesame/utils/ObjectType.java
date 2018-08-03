package fr.insee.rmes.persistance.service.sesame.utils;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SKOS;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;

public enum ObjectType {
	CONCEPT("concept", SKOS.CONCEPT,  Config.CONCEPTS_BASE_URI),
	COLLECTION("collection", SKOS.COLLECTION,  Config.COLLECTIONS_BASE_URI),
	FAMILY("family", INSEE.FAMILY, Config.FAMILIES_BASE_URI),
	SERIES("series", INSEE.SERIES, Config.SERIES_BASE_URI),
	OPERATION("operation", INSEE.OPERATION, Config.OPERATIONS_BASE_URI),
	INDICATOR("indicator", INSEE.INDICATOR, Config.INDICATORS_BASE_URI),
	UNDEFINED("undefined",null, "");
	
	

	
	private String labelType;
	private URI uri;
	private String baseUri;

	ObjectType(String label, URI uri, String baseUri){
		this.labelType=label;
		this.uri=uri;
		this.baseUri=baseUri;
	}

	public URI getUri() {
		return this.uri;
	}
	
	public String getLabelType() {
		return this.labelType;
	}
	
	public String getBaseUri() {
		return Config.BASE_URI_GESTION + this.baseUri;
	}
	
	
	private static Map<String, ObjectType> lookupLabel = new HashMap<String, ObjectType>();
	private static Map<URI, ObjectType> lookupUri = new HashMap<URI, ObjectType>();

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
	public static ObjectType getEnum(String label) {
		return lookupLabel.get(label)!=null ? lookupLabel.get(label): UNDEFINED;
	}
	
	/**
	 * Get URI by label
	 * @param label
	 * @return
	 */
	public static URI getUri(String labelType) {
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
	public static ObjectType getEnum(URI uri) {
		return lookupUri.get(uri)!=null ? lookupUri.get(uri): UNDEFINED;
	}
	
	/**
	 * Get label by URI
	 * @param labelType
	 * @return
	 */
	public static String getLabelType(URI uri) {
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

