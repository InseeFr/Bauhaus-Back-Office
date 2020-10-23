SELECT DISTINCT ?id ?label ?idSims (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) 
	WHERE {   
		GRAPH <${OPERATIONS_GRAPH}> { 
			 ?series a insee:StatisticalOperationSeries .   
			 ?series skos:prefLabel ?label . 
			 FILTER (lang(?label) = '${LG1}') 
			 BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . 
			 OPTIONAL{ 
			 ?report rdf:type sdmx-mm:MetadataReport .   
			 ?report sdmx-mm:target ?series 
			 BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .   
			 } 
			 OPTIONAL{
			 ?series skos:altLabel ?altLabelLg1 .   
			 FILTER (lang(?altLabelLg1) = '${LG1}') 
			 }   
		} 
	 } 
	 GROUP BY ?id ?label ?idSims   
	 ORDER BY ?label