SELECT ?id ?labelLg1 ?labelLg2
FROM <${OPERATIONS_GRAPH}>
WHERE {
    ?family dcterms:hasPart ?uri .
    ?uri skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}') .
    ?uri skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') .
    BIND(STRAFTER(STR(?uri),'/operations/serie/') AS ?id) .
    FILTER(STRENDS(STR(?family),'/operations/famille/${ID}')) .
}
ORDER BY ?id