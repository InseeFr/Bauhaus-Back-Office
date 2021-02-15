SELECT DISTINCT ?id ?modified ?isValidated
FROM <${CONCEPTS_GRAPH}>
WHERE {
    ?concept rdf:type skos:Concept .
    BIND(STRAFTER(STR(?concept),'/concepts/definition/') AS ?id) .
    ?concept dcterms:modified ?modified .
    ?concept insee:isValidated ?isValidated .
}