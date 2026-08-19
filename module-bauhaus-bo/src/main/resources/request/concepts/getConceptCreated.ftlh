SELECT ?created
WHERE {
    GRAPH ${CONCEPTS_GRAPH} {
        ?concept dcterms:created ?created .
        FILTER(STRENDS(STR(?concept),${CONCEPT_URI_PATTERN})) .
    }
}
LIMIT 1
