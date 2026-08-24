SELECT ?type ?created
FROM ${CODES_LISTS_GRAPH}
WHERE {
    ${IRI} rdf:type ?type .
    FILTER (?type = skos:ConceptScheme || ?type = skos:Collection)
    OPTIONAL {
        ${IRI} dcterms:created ?created .
    }
}
