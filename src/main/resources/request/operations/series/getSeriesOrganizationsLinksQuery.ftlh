SELECT DISTINCT ?id ?labelLg1 ?labelLg2 
<#if ID_SERIES = ""> 
	?idSerie
</#if>
		FROM <${OPERATIONS_GRAPH}> 
		FROM <${ORGANIZATIONS_GRAPH}> 
		FROM <${ORG_INSEE_GRAPH}> 
	WHERE {   
				?series a insee:StatisticalOperationSeries . 
				?series <${LINK_PREDICATE}> ?uriLinked .   

				?uriLinked skos:prefLabel ?labelLg1 .  
				FILTER (lang(?labelLg1) = '${LG1}') .  
				
				OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . 
				FILTER (lang(?labelLg2) = '${LG2}')} . 

				?uriLinked dcterms:identifier ?id
					
				<#if ID_SERIES != ""> 
					 	FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . 
				</#if> 
				<#if ID_SERIES = ""> 
					BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?idSerie) .
				</#if>
			}   
			ORDER BY ?labelLg1