SELECT ?closeMatch
FROM <${CONCEPTS_GRAPH}>
WHERE {
    ?concept skos:notation "${CONCEPT_ID}" .
    ?concept skos:closeMatch ?closeMatch .
}