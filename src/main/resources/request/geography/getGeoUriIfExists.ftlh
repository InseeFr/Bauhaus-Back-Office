SELECT ?uri 
FROM <${COG_GRAPH}>
FROM <${GEO_SIMS_GRAPH}>
 WHERE { 
			?uri igeo:nom ?labelLg1 ;
				 igeo:codeINSEE ?code .
			
			FILTER(REPLACE( STR(?uri) , '(.*/)(\\w.+$)', '$2' ) = "${id}")		
}