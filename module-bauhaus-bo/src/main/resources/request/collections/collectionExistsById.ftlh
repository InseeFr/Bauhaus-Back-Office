ASK {
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection), '/concepts/definitions/${ID}')) .
}
