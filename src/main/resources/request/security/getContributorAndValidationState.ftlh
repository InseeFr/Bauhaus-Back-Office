SELECT ?contributor ?validationState
FROM <${GRAPH}>
WHERE {
    ?object dc:contributor ?contributor .
    ?object insee:validationState ?validationState .
    ?object dcterms:identifier "${ID}"
}