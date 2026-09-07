ASK
WHERE {
    GRAPH ${PRODUCTS_GRAPH} {
        ?uri ?b ?c .
        FILTER(STRENDS(STR(?uri),${INDICATOR_URI_SUFFIX})) .
    }
}
