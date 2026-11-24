SELECT ?contributor
WHERE {
    GRAPH ${OPERATIONS_GRAPH} {
        ${FAMILY} dc:contributor ?contributor .
    }
}
