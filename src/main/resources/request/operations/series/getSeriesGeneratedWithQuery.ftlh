SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2 
	FROM <${PRODUCTS_GRAPH}>  
				WHERE { 
				?uri prov:wasGeneratedBy ?series . 
				?uri skos:prefLabel ?labelLg1 . 
				FILTER (lang(?labelLg1) = '${LG1}') .
				?uri skos:prefLabel ?labelLg2 . 
				FILTER (lang(?labelLg2) = '${LG2}') .
				?uri rdf:type ?typeOfObject . 
				BIND(REPLACE( STR(?uri) , '(.*/)(\\\\w+$)', '$2' ) AS ?id) . 
				FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . 
				}
				ORDER BY ?id