<#-- DATASET_URI est une IRI complète (<…>), échappée par SparqlLiterals. -->
SELECT ?contributors
WHERE {
	?catalogRecord foaf:primaryTopic ${DATASET_URI} .
	?catalogRecord dc:contributor ?contributors
}
