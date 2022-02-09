SELECT DISTINCT ?id ?labelLg1 ?labelLg2 ?idSims
WHERE {
    ?serie a insee:StatisticalOperationSeries .
    ?family dcterms:hasPart ?serie
    FILTER(STRENDS(STR(?family),'/operations/famille/${ID_FAMILY}')) .

    ?serie skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')

    ?serie skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}')

    BIND(STRAFTER(STR(?serie),'/operations/serie/') AS ?id) .
    ?report rdf:type sdmx-mm:MetadataReport .
    ?report sdmx-mm:target ?serie
    BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
}
GROUP BY ?id ?labelLg1 ?labelLg2 ?idSims
ORDER BY ?labelLg1