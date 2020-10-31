SELECT ?id ?labelLg1 ?labelLg2 

	FROM <${OPERATIONS_GRAPH}> 

	WHERE { 
				?family dcterms:hasPart ?series . 
				?family skos:prefLabel ?labelLg1 . 
				FILTER (lang(?labelLg1) = '${LG1}') . 
				?family skos:prefLabel ?labelLg2 . 
				FILTER (lang(?labelLg2) = '${LG2}') . 
				BIND(STRAFTER(STR(?family),'/famille/') AS ?id) . 

				FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . }