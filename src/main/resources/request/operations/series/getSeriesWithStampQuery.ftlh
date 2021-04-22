SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) 
	
	WHERE { 
		GRAPH <${OPERATIONS_GRAPH}> { 
			 ?series a insee:StatisticalOperationSeries .   
			 ?series skos:prefLabel ?label . 
			 ?series dc:creator ?creators 
			 FILTER (lang(?label) = '${LG1}') 
			 FILTER (STR(?creators) = "${STAMP}") 
			 BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . 
			 OPTIONAL{
			 	?series skos:altLabel ?altLabelLg1 .   
			 	FILTER (lang(?altLabelLg1) = '${LG1}') 
			 }
			 
		}
	}   
	GROUP BY ?id ?label   
	ORDER BY ?label