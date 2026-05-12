SELECT ?created
WHERE {
    GRAPH <${CONCEPTS_GRAPH}> {
        ?concept dcterms:created ?created .
        FILTER(STRENDS(STR(?concept),'/concepts/definition/${ID}')) .
    }
}
LIMIT 1
