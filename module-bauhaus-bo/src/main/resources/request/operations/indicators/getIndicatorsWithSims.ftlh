SELECT DISTINCT ?labelLg1 ?idSims
WHERE {
    ?indic a insee:StatisticalIndicator .
    ?indic skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')
    ?report rdf:type sdmx-mm:MetadataReport .
    ?report sdmx-mm:target ?indic
    BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
}
GROUP BY ?labelLg1 ?idSims
ORDER BY ?labelLg1
