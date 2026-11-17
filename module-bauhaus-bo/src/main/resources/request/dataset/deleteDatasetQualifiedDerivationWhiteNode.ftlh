DELETE {
    GRAPH ${DATASET_GRAPH} {
        ?uri prov:qualifiedDerivation ?node .
        ?node ?p ?o .
    }
}
WHERE {
    GRAPH ${DATASET_GRAPH} {
        ?uri rdf:type dcat:Dataset ;
             dcterms:identifier ${ID} ;
             prov:qualifiedDerivation ?node .
        ?node ?p ?o .
    }
}
