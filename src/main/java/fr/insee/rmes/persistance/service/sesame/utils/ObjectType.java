package fr.insee.rmes.persistance.service.sesame.utils;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;

public enum ObjectType {
	FAMILY("family", INSEE.FAMILY),
	SERIES("series", INSEE.SERIES),
	OPERATION("operation", INSEE.OPERATION),
	INDICATOR("indicator", INSEE.INDICATOR),
	UNDEFINED("undefined",null);
	
	private String label;
	private URI uri;

	ObjectType(String label, URI uri){
		this.label=label;
		this.uri=uri;
	}

	public URI getUri() {
		return this.uri;
	}
	
	public String getLabel() {
		return this.label;
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

