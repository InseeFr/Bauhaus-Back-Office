package fr.insee.rmes.model.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import fr.insee.rmes.persistance.ontologies.DCMITYPE;
import fr.insee.rmes.persistance.ontologies.ORG;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;


public enum RangeType {
	
	STRING(XMLSchema.STRING, "TEXT"),
	RICHTEXT(DCMITYPE.TEXT, "RICH_TEXT"),
	ATTRIBUTE(SDMX_MM.REPORTED_ATTRIBUTE, "REPORTED_ATTRIBUTE"),
	DATE(XMLSchema.DATE, "DATE"),
	ORGANIZATION(ORG.ORGANIZATION,"ORGANIZATION"),
	CODELIST(null,"CODE_LIST"),
	UNDEFINED(null,"undefined");
	
	

	private IRI rdfType;
	private String jsonType;
	
	
	private RangeType(IRI rdfType, String jsonType) {
		this.rdfType = rdfType;
		this.jsonType = jsonType;
	}
	
	public IRI getRdfType() {
		return rdfType;
	}

	public String getJsonType() {
		return jsonType;
	}
	
	
	private static Map<IRI, RangeType> lookupRdfType = new HashMap<>();
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
	public static RangeType getEnumByRdfType(IRI rdfType) {
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

