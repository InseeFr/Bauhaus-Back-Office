SELECT ?id
FROM <${STRUCTURES_GRAPH}>
WHERE {
	?s a <http://purl.org/linked-data/cube#DataStructureDefinition> ;
    	dcterms:identifier ?idFull .
    BIND (xsd:integer(REPLACE(str(?idFull), "dsd", "")) AS ?id)
}
ORDER BY DESC(?id)
LIMIT 1
