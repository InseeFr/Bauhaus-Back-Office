SELECT ?contributors ?validationState
WHERE {
	?dataset dcat:Distribution ${DISTRIBUTION_GRAPH_URI?no_esc} .
	?catalogRecord foaf:primaryTopic ?dataset .
	?catalogRecord dc:contributor ?contributors .
}