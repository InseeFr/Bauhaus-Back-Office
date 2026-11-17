<#-- Textes de rubriques qui citent exactement le document ou lien d'IRI URI. -->
SELECT DISTINCT ?text
WHERE {
	GRAPH ?g {
		?text insee:additionalMaterial/rdf:rest*/rdf:first ${URI} .
	}
}
