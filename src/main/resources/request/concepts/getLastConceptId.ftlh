SELECT ?notation
WHERE {
    GRAPH <${CONCEPTS_GRAPH}> {

		?concept skos:notation ?notation  .
		BIND(SUBSTR( STR(?notation) , 2 ) AS ?id) . }
		}
		ORDER BY DESC(xsd:integer(?id))
		LIMIT 1
