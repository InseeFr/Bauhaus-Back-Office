SELECT ?keyword (lang(?keyword) AS ?lang)
FROM <${DATASET_GRAPH}>
WHERE {
?uri rdf:type dcat:Dataset .
?uri dcterms:identifier '${ID}' .
?uri dcat:keyword ?keyword .
}