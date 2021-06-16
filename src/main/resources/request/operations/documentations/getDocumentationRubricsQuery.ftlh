SELECT ?idAttribute ?value ?labelLg1 ?labelLg2 ?codeList ?rangeType ?hasDocLg1 ?hasDocLg2 ?maxOccurs ?uri
	FROM <${DOCUMENTATIONS_GRAPH}/${idSims}>
	FROM <${CODELIST_GRAPH}> 
	FROM <${ORGANIZATIONS_GRAPH}>
	FROM <${ORG_INSEE_GRAPH}>

    FROM NAMED  <${MSD_GRAPH}> 

WHERE { 
			?report rdf:type sdmx-mm:MetadataReport .
			?reportAttr sdmx-mm:metadataReport ?report .


 		<#-- RangeType.DATE : value -->
		{{
			?reportAttr ?attr ?value . 
			FILTER ( datatype(?value) = <${DATE.rdfType}> ) 
			BIND('${DATE.jsonType}' AS ?rangeType) .
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.*$)', '$2' ) AS ?idAttribute) . 

		} 
			
		<#-- RangeType.RICHTEXT : label, hasDoc -->
		UNION {
			?reportAttr ?attr ?temp .
			?temp rdf:type dcmitype:Text .		
		
			OPTIONAL{ 
			 ?reportAttr ?attr ?textLg1 .
			 ?textLg1 rdf:type dcmitype:Text .
			 ?textLg1 dcterms:language <${LG1_CL}> .
			 ?textLg1 rdf:value ?labelLg1 .
			}
			 BIND(EXISTS{?textLg1 insee:additionalMaterial ?docLg1} AS ?hasDocLg1)

			 OPTIONAL {
				 ?reportAttr ?attr ?textLg2 .
				 ?textLg2 rdf:type dcmitype:Text .
				 ?textLg2 dcterms:language <${LG2_CL}> .

						OPTIONAL{ 
							?textLg2 rdf:value ?labelLg2 
						}
						BIND(EXISTS{?textLg2 insee:additionalMaterial ?docLg2} AS ?hasDocLg2)
			 }
			 
			 BIND(REPLACE( STR(?attr) , '(.*/)(\\w.*$)', '$2' ) AS ?idAttribute) . 

			 BIND('${RICHTEXT.jsonType}' AS ?rangeType) . 
		} 
		
		<#-- RangeType.STRING : label -->
		UNION {
			 ?reportAttr ?attr ?labelLg1 .
			 FILTER(lang(?labelLg1) = '${LG1}') 
			 OPTIONAL{?reportAttr ?attr ?labelLg2 .
			 	FILTER(lang(?labelLg2) = '${LG2}') } 
			 BIND('${STRING.jsonType}' AS ?rangeType) . 
			 BIND(REPLACE( STR(?attr) , '(.*/)(\\w.*$)', '$2' ) AS ?idAttribute) . 

		} 
			
		<#-- RangeType.CODELIST : value, codelist, maxoccurs -->
		UNION {
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.*$)', '$2' ) AS ?idAttribute) . 
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
			?reportAttr ?attr ?organisationUri . 
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.*$)', '$2' ) AS ?idAttribute) . 
			?organisationUri dcterms:identifier ?value .
			BIND('${ORGANIZATION.jsonType}' AS ?rangeType) .
		} 
		
		<#-- RangeType.GEOGRAPHY : value (uri), label -->
		UNION {
			?reportAttr ?attr ?uri . 
			GRAPH  <${MSD_GRAPH}>  {
					?attr rdfs:range geo:Feature .
				}
			BIND(REPLACE( STR(?attr) , '(.*/)(\\w.*$)', '$2' ) AS ?idAttribute) .
			BIND('${GEOGRAPHY.jsonType}' AS ?rangeType) .
		} }
			
}