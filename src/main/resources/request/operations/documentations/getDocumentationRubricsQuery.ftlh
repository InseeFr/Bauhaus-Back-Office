SELECT ?idAttribute ?value ?labelLg1 ?labelLg2 ?codeList ?rangeType ?hasDoc
	FROM <http://rdf.insee.fr/graphes/qualite/rapport/${idSims}>
	FROM <http://rdf.insee.fr/graphes/codes> 
	FROM <http://rdf.insee.fr/graphes/organisations>
 
WHERE { 
 		<#-- RangeType.DATE : value -->
		{
			?report rdf:type sdmx-mm:MetadataReport .
			?reportAttr sdmx-mm:metadataReport ?report .
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			?reportAttr ?attr ?value . 
			FILTER ( datatype(?value) = <${DATE.rdfType}> ) 
			BIND('${DATE.jsonType}' AS ?rangeType) .
		} 
			
		<#-- RangeType.RICHTEXT : label -->
		UNION {
			 ?report rdf:type sdmx-mm:MetadataReport .
			 ?reportAttr sdmx-mm:metadataReport ?report .
			 ?reportAttr ?attr ?text .
			 ?text rdf:type dcmitype:Text
			 BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			 
			 OPTIONAL{ ?text rdf:value ?labelLg1 .
				 FILTER(lang(?labelLg1) = '${LG1}') 
			 }
			 OPTIONAL{?text rdf:value ?labelLg2 .
			 	FILTER(lang(?labelLg2) = '${LG2}') 
			 } 
			 BIND(EXISTS{?text insee:additionalMaterial ?doc} AS ?hasDoc)

			 BIND('${RICHTEXT.jsonType}' AS ?rangeType) . 
		} 
		
		<#-- RangeType.STRING : label -->
		UNION {
			 ?report rdf:type sdmx-mm:MetadataReport .
			 ?reportAttr sdmx-mm:metadataReport ?report .
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			 ?reportAttr ?attr ?labelLg1 .
			 FILTER(lang(?labelLg1) = '${LG1}') 
			 OPTIONAL{?reportAttr ?attr ?labelLg2 .
			 FILTER(lang(?labelLg2) = '${LG2}') } 
			 BIND('${STRING.jsonType}' AS ?rangeType) . 
		} 
			
		<#-- RangeType.CODELIST : value -->
		UNION {
			?report rdf:type sdmx-mm:MetadataReport .
			?reportAttr sdmx-mm:metadataReport ?report .
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			?reportAttr ?attr ?codeUri . 
			?codeUri skos:notation ?value .
			?codeUri skos:inScheme ?listUri .
			?listUri skos:notation ?codeList .
			BIND('${CODELIST.jsonType}' AS ?rangeType) .
		} 
		UNION {
			?report rdf:type sdmx-mm:MetadataReport .
			?reportAttr sdmx-mm:metadataReport ?report .
			?reportAttr ?attr ?organisationUri . 
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			?organisationUri dcterms:identifier ?value .
			BIND('${ORGANIZATION.jsonType}' AS ?rangeType) .
		} 
			
}