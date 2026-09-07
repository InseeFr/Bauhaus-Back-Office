ASK {
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection), ${COLLECTION_URI_SUFFIX})) .
}
