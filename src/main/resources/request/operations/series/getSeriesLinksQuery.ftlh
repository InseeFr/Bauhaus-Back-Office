SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2   
	WHERE {   
				?series <${LINK_PREDICATE}> ?uriLinked .   
				?uriLinked skos:prefLabel ?labelLg1 .  
				FILTER (lang(?labelLg1) = ' ${LG1}  ') .  
				OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 . 
				FILTER (lang(?labelLg2) = '  ${LG2}  ')} .   
				?uriLinked rdf:type ?typeOfObject . 
				BIND(REPLACE( STR(?uriLinked) , '(.*/)(\\\\w$)', '$2' ) AS ?id) . 
				FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . 
			}   
			ORDER BY ?labelLg1