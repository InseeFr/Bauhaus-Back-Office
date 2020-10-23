SELECT ?id ?labelLg1 ?labelLg2 
		FROM <${OPERATIONS_GRAPH}> 
		FROM <${ORGANIZATIONS_GRAPH}> 
		FROM <${ORG_INSEE_GRAPH}> 
	WHERE { 
		?series <${LINK_PREDICATE}> ?uri .
		?uri dcterms:identifier  ?id . 
		?uri skos:prefLabel ?labelLg1 .
		FILTER (lang(?labelLg1) = '${LG1}') .
		OPTIONAL {?uri skos:prefLabel ?labelLg2 . 
		FILTER (lang(?labelLg2) = '${LG2}')
		} .
		FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) .
		} 
		ORDER BY ?id