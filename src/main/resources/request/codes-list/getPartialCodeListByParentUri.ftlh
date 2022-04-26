SELECT ?iri
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?iri prov:wasDerivedFrom <${IRI}>.
}