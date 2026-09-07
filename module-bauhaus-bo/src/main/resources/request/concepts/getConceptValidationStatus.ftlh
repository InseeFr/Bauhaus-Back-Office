SELECT ?state
FROM ${CONCEPTS_GRAPH}
WHERE {
    ?concept skos:notation ${ID} .
    ?concept insee:validationState ?state .
}