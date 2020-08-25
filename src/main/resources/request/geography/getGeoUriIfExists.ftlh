SELECT ?uri 
FROM <${COG_GRAPH}>
FROM <${GEO_SIMS_GRAPH}>
 WHERE { 
			?uri igeo:nom|rdfs:label ?labelLg1 .
			OPTIONAL{	?uri igeo:codeINSEE ?code .
				}			
			FILTER(REPLACE( STR(?uri) , '(.*/)(\\w.+$)', '$2' ) = "${id}")		
}