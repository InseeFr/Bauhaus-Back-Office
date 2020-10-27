SELECT ?id ?labelLg1 ?labelLg2   
		FROM <${OPERATIONS_GRAPH}> 
		FROM <${ORGANIZATIONS_GRAPH}> 
		FROM <${ORG_INSEE_GRAPH}> 
	WHERE {   
				?series <${LINK_PREDICATE}> ?uriLinked .   
				?uriLinked skos:prefLabel ?labelLg1 .  
				FILTER (lang(?labelLg1) = '${LG1}') .  
				OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . 
				FILTER (lang(?labelLg2) = '${LG2}')} .   
				?uriLinked dcterms:identifier  ?id . 
				FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . 
			}   
			ORDER BY ?labelLg1