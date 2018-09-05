package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import fr.insee.rmes.persistance.service.sesame.ontologies.ORG;
import fr.insee.rmes.persistance.service.sesame.ontologies.SDMX_MM;
import fr.insee.rmes.persistance.service.sesame.ontologies.XSD;


public enum RangeType {
	
	STRING(XSD.STRING, "TEXT"),
	ATTRIBUTE(SDMX_MM.REPORTED_ATTRIBUTE, "REPORTED_ATTRIBUTE"),
	DATE(XSD.DATE, "DATE"),
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
	
	
	private static Map<URI, RangeType> lookupRdfType = new HashMap<URI, RangeType>();

	static {
		// Populate out lookup when enum is created
		for (RangeType e : RangeType.values()) {
			lookupRdfType.put(e.getRdfType(), e);
		}
	}
	
	/**
	 * Get Enum type by label
	 * @param label
	 * @return
	 */
	public static RangeType getEnumByRdfType(URI rdfType) {
		if (rdfType.getNamespace().contains("/code")){
			return RangeType.CODELIST;
		}
		return lookupRdfType.get(rdfType)!=null ? lookupRdfType.get(rdfType):UNDEFINED;
	}
	
	

	
}

