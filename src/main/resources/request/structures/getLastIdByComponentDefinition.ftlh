SELECT ?id
FROM <${STRUCTURES_GRAPH}>
WHERE {
	?s a qb:ComponentSpecification .
    ?s dcterms:identifier ?idp .
    BIND (xsd:integer(REPLACE(str(?idp), "^cs", "")) AS ?id)
}
ORDER BY DESC(?id)
LIMIT 1