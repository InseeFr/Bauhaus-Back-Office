SELECT ?closeMatch ?closeMatchNotation
FROM <${OBJECT_GRAPH}>
WHERE {
    ?concept skos:notation "${OBJECT_ID}" .
    ?concept skos:closeMatch ?closeMatch .
    ?closeMatch skos:notation ?closeMatchNotation .
}