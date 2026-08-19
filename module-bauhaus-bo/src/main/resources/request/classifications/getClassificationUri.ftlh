SELECT ?iri
WHERE {
    ?iri rdf:type skos:ConceptScheme .
    FILTER(REGEX(STR(?iri),${CLASSIFICATION_URI_PATTERN})) .
}