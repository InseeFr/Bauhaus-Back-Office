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
	INDICATOR("indicator", INSEE.INDICATOR, ""),
	UNDEFINED("undefined",null, "");
	
	

	
	private String label;
	private URI uri;
	private String baseUri;

	ObjectType(String label, URI uri, String baseUri){
		this.label=label;
		this.uri=uri;
		this.baseUri=baseUri;
	}

	public URI getUri() {
		return this.uri;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public String getBaseUri() {
		return Config.BASE_URI_GESTION + this.baseUri;
	}
	
	
	private static Map<String, ObjectType> lookupLabel = new HashMap<String, ObjectType>();
	private static Map<URI, ObjectType> lookupUri = new HashMap<URI, ObjectType>();

	static {
		// Populate out lookup when enum is created
		for (ObjectType e : ObjectType.values()) {
			lookupLabel.put(e.getLabel(), e);
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
	public static URI getUri(String label) {
		return getEnum(label).uri;
	}

	/**
	 * Get Enum type by URI
	 * @param label
	 * @return
	 */
	public static ObjectType getEnum(URI uri) {
		return lookupUri.get(uri)!=null ? lookupUri.get(uri): UNDEFINED;
	}
	
	/**
	 * Get label by URI
	 * @param label
	 * @return
	 */
	public static String getLabel(URI uri) {
		return getEnum(uri).label;
	}

}

