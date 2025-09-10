SELECT ?state
FROM <${STRUCTURES_GRAPH}>
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?structureOrComponent insee:validationState ?state .
    FILTER(STRENDS(STR(?structureOrComponent), '${id}'))
}