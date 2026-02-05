SELECT ?contributors
FROM <${GRAPH}>
WHERE {
    <${IRI}> dc:contributor ?contributors .
}