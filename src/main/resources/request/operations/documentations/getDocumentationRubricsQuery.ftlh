SELECT ?idAttribute ?value ?labelLg1 ?labelLg2 ?codeList ?rangeType ?hasDoc ?maxOccurs
	FROM <${DOCUMENTATIONS_GRAPH}/${idSims}>
	FROM <${CODELIST_GRAPH}> 
	FROM <${ORGANIZATIONS_GRAPH}>
	FROM <${ORG_INSEE_GRAPH}>

    FROM NAMED  <${MSD_GRAPH}> 
 	FROM NAMED  <${COG_GRAPH}>
	FROM NAMED  <${GEO_SIMS_GRAPH}>
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
			
		<#-- RangeType.RICHTEXT : label, hasDoc -->
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
			
		<#-- RangeType.CODELIST : value, codelist, maxoccurs -->
		UNION {
			?report rdf:type sdmx-mm:MetadataReport .
			?reportAttr sdmx-mm:metadataReport ?report .
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			?reportAttr ?attr ?codeUri . 
			?codeUri skos:notation ?value .
			?codeUri skos:inScheme ?listUri .
			?listUri skos:notation ?codeList .
			BIND('${CODELIST.jsonType}' AS ?rangeType) .
						
			OPTIONAL {
				GRAPH  <${MSD_GRAPH}>  {
					?mas sdmx-mm:metadataAttributeProperty ?attr . 
					?mas sdmx-mm:maxOccurs ?maxOccurs
				}
			} .
		} 
		<#-- RangeType.ORGANIZATION : value -->
		UNION {
			?report rdf:type sdmx-mm:MetadataReport .
			?reportAttr sdmx-mm:metadataReport ?report .
			?reportAttr ?attr ?organisationUri . 
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) . 
			?organisationUri dcterms:identifier ?value .
			BIND('${ORGANIZATION.jsonType}' AS ?rangeType) .
		} 
		
		<#-- RangeType.GEOGRAPHY : value (uri), label -->
		UNION {
			?report rdf:type sdmx-mm:MetadataReport .
			?reportAttr sdmx-mm:metadataReport ?report .
			?reportAttr ?attr ?value . 
			GRAPH  <${MSD_GRAPH}>  {
					?attr rdfs:range geo:Feature .
				}
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.+$)', '$2' ) AS ?idAttribute) .
			GRAPH  ?g {
				?value skos:prefLabel|igeo:name|rdfs:label ?labelLg1 .
			}
			BIND('${GEOGRAPHY.jsonType}' AS ?rangeType) .
		} 
			
}