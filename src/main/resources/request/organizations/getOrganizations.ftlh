SELECT DISTINCT ?iri ?id ?label ?altLabel ?labelLg2
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
    ?iri dcterms:identifier ?id .

    OPTIONAL { ?iri skos:prefLabel ?label .
    FILTER (lang(?label) = '${LG1}')}
    OPTIONAL {?iri skos:altLabel ?altLabel .}

    OPTIONAL {
        ?iri skos:prefLabel ?label2 .
        FILTER (lang(?label2) = '${LG2}')
    } .

    BIND(if(bound(?label2), ?label2, ?label) AS ?labelLg2)
}
GROUP BY ?iri ?id ?label ?altLabel ?labelLg2
ORDER BY ?label