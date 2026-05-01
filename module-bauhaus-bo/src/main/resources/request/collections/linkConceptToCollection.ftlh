INSERT {
    GRAPH <${GRAPH}> {
        ?collection skos:member <${CONCEPT_URI}> .
    }
}
WHERE {
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection), '/concepts/definitions/${COLLECTION_ID}')) .
}
