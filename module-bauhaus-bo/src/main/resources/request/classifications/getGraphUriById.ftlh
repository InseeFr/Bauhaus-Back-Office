SELECT DISTINCT ?graph ?uri 
WHERE { GRAPH ?graph { 
	?uri rdf:type skos:ConceptScheme .
	FILTER(REGEX(STR(?uri),${CLASSIFICATION_URI_PATTERN})) .
	}} 
ORDER BY ?graph ?uri