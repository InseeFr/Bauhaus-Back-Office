package fr.insee.rmes.modele.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import fr.insee.rmes.persistance.ontologies.DCMITYPE;
import fr.insee.rmes.persistance.ontologies.ORG;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.ontologies.XSD;


public enum RangeType {
	
	STRING(XSD.STRING, "TEXT"),
	RICHTEXT(DCMITYPE.TEXT, "RICH_TEXT"),
	ATTRIBUTE(SDMX_MM.REPORTED_ATTRIBUTE, "REPORTED_ATTRIBUTE"),
	DATE(XSD.DATETIME, "DATE"),
	ORGANIZATION(ORG.ORGANIZATION,"ORGANIZATION"),
	CODELIST(null,"CODE_LIST"),
	UNDEFINED(null,"undefined");
	
	

	private URI rdfType;
	private String jsonType;
	
	
	private RangeType(URI rdfType, String jsonType) {
		this.rdfType = rdfType;
		this.jsonType = jsonType;
	}
	
	public URI getRdfType() {
		return rdfType;
	}

	public String getJsonType() {
		return jsonType;
	}
	
	
	private static Map<URI, RangeType> lookupRdfType = new HashMap<>();
	private static Map<String, RangeType> lookupJsonType = new HashMap<>();


	static {
		// Populate out lookup when enum is created
		for (RangeType e : RangeType.values()) {
			lookupRdfType.put(e.getRdfType(), e);
			lookupJsonType.put(e.getJsonType(), e);
		}
	}
	
	/**
	 * Get Enum type by RDF type
	 * @param rdfType
	 * @return
	 */
	public static RangeType getEnumByRdfType(URI rdfType) {
		if (rdfType.getNamespace().contains("/code")){
			return RangeType.CODELIST;
		}
		return lookupRdfType.get(rdfType)!=null ? lookupRdfType.get(rdfType):UNDEFINED;
	}
	
	/**
	 * Get Enum type by Json Type
	 * @param jsonType
	 * @return
	 */
	public static RangeType getEnumByJsonType(String jsonType) {
		return lookupJsonType.get(jsonType)!=null ? lookupJsonType.get(jsonType):UNDEFINED;
	}
	

	
}

