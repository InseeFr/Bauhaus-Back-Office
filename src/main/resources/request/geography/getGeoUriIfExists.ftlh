SELECT ?uri 
FROM <${COG_GRAPH}>
FROM <${GEO_SIMS_GRAPH}>
 WHERE { 

    ?uri (igeo:nom|rdfs:label|skos:prefLabel) ?labelLg1 .
    OPTIONAL{
			?uri igeo:codeINSEE ?code .
    }		
			FILTER(REPLACE( STR(?uri) , '(.*/)(\\w.+$)', '$2' ) = "${id}")		
}