SELECT ?iri
FROM <${DATASET_GRAPH}>
WHERE {
    ?dataset dcterms:identifier '${ID}' ;
             prov:wasGeneratedBy ?iri .
}
