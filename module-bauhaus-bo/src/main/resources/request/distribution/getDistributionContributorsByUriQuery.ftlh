<#-- DISTRIBUTION_URI est une IRI complète (<…>), échappée par SparqlLiterals. -->
SELECT ?contributors ?validationState
WHERE {
	?dataset dcat:distribution ${DISTRIBUTION_URI} .
	?catalogRecord foaf:primaryTopic ?dataset .
	?catalogRecord dc:contributor ?contributors .
}
