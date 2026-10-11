<#-- Rubriques de rapports qualité qui citent le document ou lien d'IRI URI. Un graphe par
     rapport : GRAPH ?g, sans FROM. -->
SELECT DISTINCT ?simsId ?labelLg1 ?labelLg2 ?rubricId
WHERE {
	GRAPH ?g {
		?text insee:additionalMaterial/rdf:rest*/rdf:first ${URI} .
		?rubric ?property ?text .
		?rubric sdmx-mm:metadataReport ?sims .
		OPTIONAL { ?sims rdfs:label ?labelLg1 . FILTER(lang(?labelLg1) = ${LG1}) }
		OPTIONAL { ?sims rdfs:label ?labelLg2 . FILTER(lang(?labelLg2) = ${LG2}) }
	}
	BIND(STRAFTER(STR(?sims), "/qualite/rapport/") AS ?simsId)
	BIND(REPLACE(STR(?rubric), "^.*/", "") AS ?rubricId)
}
ORDER BY ?simsId ?rubricId
