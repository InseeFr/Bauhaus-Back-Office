DELETE WHERE {
GRAPH <${DATASET_GRAPH}>
    {
?uri rdf:type dcat:Dataset .
    ?uri dcterms:temporal ?node .
    ?uri dcterms:identifier '${ID}' .
    ?node rdf:type dcterms:PeriodOfTime .
    }
}