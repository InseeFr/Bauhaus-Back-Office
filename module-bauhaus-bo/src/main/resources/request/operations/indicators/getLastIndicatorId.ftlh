SELECT ?id
WHERE {
    GRAPH <${PRODUCTS_GRAPH}> {
        ?uri ?b ?c .
        BIND(REPLACE( STR(?uri) , '(.*/)(\\w+$)', '$2' ) AS ?id) .
        BIND(SUBSTR( ?id , 2 ) AS ?intid) .
        FILTER regex(STR(?uri),'/${PRODUCT_BASE_URI}/') .
    }
}
ORDER BY DESC(xsd:integer(?intid))
LIMIT 1
