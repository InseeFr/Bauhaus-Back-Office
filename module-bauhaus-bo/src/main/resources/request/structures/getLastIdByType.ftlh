SELECT ?id
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
	?s a <${TYPE}>
	BIND (xsd:integer(REPLACE(str(?s), "^.*${NAMESPACE}/(.)", "")) AS ?id)
}
ORDER BY DESC(?id)
LIMIT 1