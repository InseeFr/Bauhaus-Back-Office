SELECT ?linkedDocument
FROM <${DATASET_GRAPH}>
WHERE {
?uri rdf:type dcat:Dataset .
?uri dcterms:identifier '${ID}' .
?uri dcterms:relation ?linkedDocument .
}