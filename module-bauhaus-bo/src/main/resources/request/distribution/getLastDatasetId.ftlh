SELECT ?id
FROM <${DATASET_GRAPH}>
WHERE {
    ?s a dcat:Distribution ;
       dcterms:identifier ?idFull .

    BIND (xsd:integer(REPLACE(str(?idFull), "d", "")) AS ?id)
}
ORDER BY DESC(?id)
LIMIT 1
