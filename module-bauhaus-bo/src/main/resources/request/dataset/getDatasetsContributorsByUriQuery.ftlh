
SELECT ?contributors
WHERE {
	?catalogRecord foaf:primaryTopic ${DATASET_GRAPH_URI?no_esc} .
	?catalogRecord dc:contributor ?contributors
}