ASK
WHERE {
    GRAPH <${PRODUCTS_GRAPH}> {
        ?uri ?b ?c .
        FILTER(STRENDS(STR(?uri),'/${PRODUCT_BASE_URI}/${ID}')) .
    }
}
