SELECT ?contributor ?validationState
FROM <${GRAPH}>
WHERE {
    ?object dc:contributor ?contributor .
    ?object insee:validationState ?validationState .
    ?object skos:notation "${ID}"
}