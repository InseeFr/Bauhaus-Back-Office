SELECT DISTINCT ?uriClasseOwl
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?uriClasseOwl rdf:type owl:Class .
    ?uriClasseOwl rdfs:seeAlso <${CODES_LIST}> .
}