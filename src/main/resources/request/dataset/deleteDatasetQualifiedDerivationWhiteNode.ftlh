DELETE WHERE {
GRAPH <${DATASET_GRAPH}>
    {
?uri rdf:type dcat:Dataset .
    ?uri prov:qualifiedDerivation ?node .
    ?uri dcterms:identifier '${ID}' .
    ?node rdf:type prov:Derivation .
    }
}