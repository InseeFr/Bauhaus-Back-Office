SELECT DISTINCT ?feature ?composition ?uri ?labelLg1 ?code
FROM ${COG_GRAPH}
FROM ${GEO_SIMS_GRAPH}
 WHERE {
    VALUES (?relation ?composition) {
        (geo:union "unions")
        (geo:difference "difference")
    }
    ?feature ?relation ?uri .

    ?uri (igeo:nom|rdfs:label|skos:prefLabel) ?labelLg1 .
    OPTIONAL{
			?uri igeo:codeINSEE ?code .
    }

}
ORDER BY ?feature ?code
