SELECT ?contributor
FROM <${GRAPH}>
WHERE {
    <${IRI}> ${PREDICATE} ?contributor .
}