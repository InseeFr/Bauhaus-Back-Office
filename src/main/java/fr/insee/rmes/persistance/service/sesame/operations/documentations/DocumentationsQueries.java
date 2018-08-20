package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import fr.insee.rmes.config.Config;

public class DocumentationsQueries {

	public static String msdQuery() {
		return "SELECT ?idMas ?masLabelLg1 ?masLabelLg2 ?idParent ?isPresentational \n"
		 + " FROM <http://rdf.insee.fr/graphes/def/simsv2fr> \n"
		+ " WHERE { \n"
	    + " ?reportStructure sdmx-mm:metadataAttributeSpecification ?mas . \n"
		+ "    ?mas rdfs:label ?masLabelLg1 ; \n" 
		+ "      	rdfs:label ?masLabelLg2 ; \n" 
		+ "    FILTER(lang(?masLabelLg1) = '" + Config.LG1 + "') \n" 
		+ "    FILTER(lang(?masLabelLg2) = '" + Config.LG2 + "') \n" 
		
		+ "   OPTIONAL {?mas sdmx-mm:parent ?parent } \n"
		+ "BIND(REPLACE( STR(?parent) , '(.*/)(\\\\w.+$)', '$2' ) AS ?idParent) . \n"

		+ "   OPTIONAL {?mas sdmx-mm:isPresentational ?presentational } \n"
		+ "FILTER(STRENDS(STR(?reportStructure),'/qualite/simsv2fr/reportStructure')) . \n"
		
		+ "BIND(REPLACE( STR(?mas) , '(.*/)(\\\\w.+$)', '$2' ) AS ?idMas) . \n"


		+ "  } \n"
		+ "  ORDER BY ?mas";
	}
	
	public static String getAttributeSpecificationQuery(String idMas) {
		return "SELECT ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational ?codeListNotation \n" 
				+ "  FROM <http://rdf.insee.fr/graphes/def/simsv2fr> \n" 
				+ "  FROM <http://rdf.insee.fr/graphes/codes> \n" 
				+ "  WHERE { \n" 
				+ "    ?mas rdfs:label ?masLabelLg1 ; \n" 
				+ "      	rdfs:label ?masLabelLg2 ; \n" 
				+ "    FILTER(lang(?masLabelLg1) = '" + Config.LG1 + "') \n" 
				+ "    FILTER(lang(?masLabelLg2) = '" + Config.LG2 + "') \n" 
				+ "    OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational } \n" 
								
				+ "    ?mas sdmx-mm:metadataAttributeProperty ?map . \n"
				+ "    OPTIONAL {?map rdfs:range ?range } \n" 
								
				+ "    FILTER(STRENDS(STR(?mas), '"+idMas+"')) \n"

				+ "  } \n" 
				+ "  ORDER BY ?mas";
	}
}
