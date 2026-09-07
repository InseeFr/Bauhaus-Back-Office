DELETE {
    GRAPH ${GRAPH} {
        ?collection skos:member ${CONCEPT_URI} .
    }
}
WHERE {
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection), ${COLLECTION_URI_SUFFIX})) .
    GRAPH ${GRAPH} {
        ?collection skos:member ${CONCEPT_URI} .
    }
}
