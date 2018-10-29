SELECT ?idAttribute ?value ?labelLg1 ?labelLg2 ?codeList ?rangeType 
	FROM <http://rdf.insee.fr/graphes/qualite/rapport/${idSims}>
	FROM <http://rdf.insee.fr/graphes/codes> 
 
WHERE { 
 		<#-- RangeType.DATE : value -->
		{
			?report rdf:type sdmx-mm:MetadataReport .
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			?report ?attr ?value . 
			FILTER ( datatype(?value) = <${DATE.rdfType}> ) 
			BIND('${DATE.jsonType}' AS ?rangeType) .
		} 
			
		<#-- RangeType.ATTRIBUTE : label -->
		UNION {
			 ?report rdf:type sdmx-mm:MetadataReport .
			 BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			 ?report ?attr ?node . 
			 ?node rdf:value ?labelLg1 .
			 FILTER(lang(?labelLg1) = '${LG1}') 
			 OPTIONAL{?node rdf:value ?labelLg2 .
			 FILTER(lang(?labelLg2) = '${LG2}') } 
			 BIND('${ATTRIBUTE.jsonType}' AS ?rangeType) . 
		} 
			
		<#-- RangeType.CODELIST : value -->
		UNION {
			?report rdf:type sdmx-mm:MetadataReport .
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			?report ?attr ?codeUri . 
			?codeUri skos:notation ?value .
			?codeUri skos:inScheme ?listUri .
			?listUri skos:notation ?codeList .
			BIND('${CODELIST.jsonType}' AS ?rangeType) .
		} 
			
}