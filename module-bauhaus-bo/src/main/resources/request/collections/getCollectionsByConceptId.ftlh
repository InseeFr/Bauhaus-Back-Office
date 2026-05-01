SELECT DISTINCT ?id
WHERE {
    ?collection rdf:type skos:Collection .
    BIND(STRAFTER(STR(?collection), '/concepts/definitions/') AS ?id) .
    ?collection skos:member ?member .
    ?member skos:notation ?notation .
    FILTER(STR(?notation) = "${CONCEPT_ID}") .
    FILTER(?id != "") .
}
