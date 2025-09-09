SELECT ?position ?code
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?list skos:notation '${NOTATION}' ;
    rdf:type rdf:Seq ;
    ?predicate ?uri .
    BIND(xsd:integer(STRAFTER(STR(?predicate),'#_')) AS ?position) .
    FILTER(!ISBLANK(?position))
    ?uri skos:notation ?code
}
ORDER BY ASC(xsd:integer(?position))
