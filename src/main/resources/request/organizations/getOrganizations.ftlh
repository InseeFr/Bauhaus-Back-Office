SELECT DISTINCT ?iri ?id ?label ?altLabel
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
    ?iri dcterms:identifier ?id .

    OPTIONAL { ?iri skos:prefLabel ?label .
    FILTER (lang(?label) = '${LG1}')}
    OPTIONAL {?iri skos:altLabel ?altLabel .}
}
GROUP BY ?iri ?id ?label ?altLabel
ORDER BY ?label