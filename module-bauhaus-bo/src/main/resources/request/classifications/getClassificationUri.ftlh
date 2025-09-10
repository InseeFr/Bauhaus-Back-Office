SELECT ?iri
WHERE {
    ?iri rdf:type skos:ConceptScheme .
    FILTER(REGEX(STR(?iri),'/codes/${ID}/')) .
}