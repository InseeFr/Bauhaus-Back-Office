SELECT DISTINCT ?id ?labelLg1 ?labelLg2 ?typeOfObject
		FROM <${OPERATIONS_GRAPH}> 
		FROM <${PRODUCTS_GRAPH}> 
	WHERE {   
				?series <${LINK_PREDICATE}> ?uriLinked .   

				?uriLinked skos:prefLabel ?labelLg1 .  
				FILTER (lang(?labelLg1) = '${LG1}') .  
				
				OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . 
				FILTER (lang(?labelLg2) = '${LG2}')} .   
				
				OPTIONAL {?uriLinked rdf:type ?typeOfObject .}
				
				BIND(
					IF(regex(str(?uriLinked), "/operations/serie/"),
						STRAFTER(STR(?uriLinked),'/operations/serie/'),
					IF(regex(str(?uriLinked), "/produits/indicateur/"),
						STRAFTER(STR(?uriLinked),'/produits/indicateur/'),
					STR(?uriLinked) ))
				AS ?id) .
				
				FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . 
			}   
			ORDER BY ?labelLg1