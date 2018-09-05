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

		+ "   OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational } \n"
		+ "FILTER(STRENDS(STR(?reportStructure),'/qualite/simsv2fr/reportStructure')) . \n"
		
		+ "BIND(REPLACE( STR(?mas) , '(.*/)(\\\\w.+$)', '$2' ) AS ?idMas) . \n"


		+ "  } \n"
		+ "  ORDER BY ?mas";
	}
	
	public static String getAttributeSpecificationQuery(String idMas) {
		return "SELECT ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational \n" 
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
	
	public static String getAttributesQuery() {
		return "SELECT ?id ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational \n" 
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
								
				+ "BIND(REPLACE( STR(?mas) , '(.*/)(\\\\w.+$)', '$2' ) AS ?id) . \n"

				+ "  } \n" 
				+ "  ORDER BY ?mas";
	}
	
	public static String getDocumentationTitleQuery(String idSims) {
		return "SELECT ?labelLg1 ?labelLg2 \n"
				+ "FROM <http://rdf.insee.fr/graphes/qualite/rapport/" + idSims +"> \n"
				+ " WHERE { \n"
				+ " ?report rdf:type sdmx-mm:MetadataReport ."
				+ " ?report rdfs:label ?labelLg1 "
				+ "    FILTER(lang(?labelLg1) = '" + Config.LG1 + "') \n" 
				+ " ?report rdfs:label ?labelLg2 "
				+ "    FILTER(lang(?labelLg2) = '" + Config.LG2 + "') \n" 
				
				+ "    FILTER(STRENDS(STR(?report), '"+idSims+"')) \n"
				+ "}";
		

	}
	
	public static String getDocumentationRubricsQuery(String idSims) {
		//TODO RangeType.STRING, RangeType.ORGANIZATION, RangeType.UNDEFINED
		return "SELECT ?idAttribute ?value ?labelLg1 ?labelLg2 ?codeList ?rangeType \n "
			+ "FROM <http://rdf.insee.fr/graphes/qualite/rapport/" + idSims+"> \n " 
			+" FROM <http://rdf.insee.fr/graphes/codes> \n"
			+ "WHERE { \n "
	
			//RangeType.DATE --> value
			+ "{\n"
			+ " ?report rdf:type sdmx-mm:MetadataReport .\n "
			+ " BIND(REPLACE( STR(?attr) , '(.*/)(\\\\w.+$)', '$2' ) AS ?idAttribute) . \n"
			+ " ?report ?attr ?value . "
			+ " FILTER ( datatype(?value) = <"+RangeType.DATE.getRdfType()+"> ) "
			+ "BIND('"+RangeType.DATE.getJsonType()+"' AS ?rangeType) . \n"

			+"  } \n"
			
			 
			//RangeType.ATTRIBUTE --> label
			+ " UNION {\n"
			+ " ?report rdf:type sdmx-mm:MetadataReport .\n "
			+ " BIND(REPLACE( STR(?attr) , '(.*/)(\\\\w.+$)', '$2' ) AS ?idAttribute) . \n"
			+ " ?report ?attr ?node . "
			+ " ?node rdf:value ?labelLg1 ."
			+ "    FILTER(lang(?labelLg1) = '" + Config.LG1 + "') \n" 
			+ " OPTIONAL{?node rdf:value ?labelLg2 ."
			+ "    FILTER(lang(?labelLg2) = '" + Config.LG2 + "') } \n" 
			+ "BIND('"+RangeType.ATTRIBUTE.getJsonType()+"' AS ?rangeType) . \n"

			+"  } \n"
			
			//RangeType.CODELIST --> value
			+ " UNION {\n"
			+ " ?report rdf:type sdmx-mm:MetadataReport .\n "
			+ " BIND(REPLACE( STR(?attr) , '(.*/)(\\\\w.+$)', '$2' ) AS ?idAttribute) . \n"
			+ " ?report ?attr ?codeUri . "
			+ " ?codeUri skos:notation ?value ."
			+ " ?codeUri skos:inScheme ?listUri ."
			+ " ?listUri skos:notation ?codeList ."
			+ "BIND('"+RangeType.CODELIST.getJsonType()+"' AS ?rangeType) . \n"

			+"  } \n"
			
			+"}";
		
	}
	
}
