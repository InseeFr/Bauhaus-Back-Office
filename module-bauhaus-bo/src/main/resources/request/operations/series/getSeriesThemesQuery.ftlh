SELECT ?themes
WHERE {
    GRAPH ${OPERATIONS_GRAPH} {
        ${SERIES_IRI} dcterms:subject ?themes .
    }
}
ORDER BY ?themes
