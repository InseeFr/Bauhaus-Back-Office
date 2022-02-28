SELECT DISTINCT ?graph ?uri 
WHERE { GRAPH ?graph { 
	?uri rdf:type skos:ConceptScheme .
	FILTER(REGEX(STR(?uri),'/codes/${classifId}/')) .
	}} 
ORDER BY ?graph ?uri