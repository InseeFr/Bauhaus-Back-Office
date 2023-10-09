SELECT ?id
FROM <${DATASET_GRAPH}>
WHERE {
    ?s a <http://www.w3.org/dcat#Dataset> ;
       dcterms:identifier ?idFull .

    BIND (xsd:integer(REPLACE(str(?idFull), "jd", "")) AS ?id)
}
ORDER BY DESC(?id)
LIMIT 1
