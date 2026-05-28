ASK
FROM <${CONCEPTS_GRAPH}>
WHERE {
    ?codelist skos:notation '${ID}' .
    ?codelist insee:validationState 'Validated' .
}