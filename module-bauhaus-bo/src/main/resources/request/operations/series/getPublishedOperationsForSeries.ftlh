SELECT ?operation
FROM <${OPERATIONS_GRAPH}>
WHERE {
    <${SERIES_IRI}> dcterms:hasPart ?operation .
    ?operation insee:validationState ?value
    FILTER(?value != 'Unpublished') .
}