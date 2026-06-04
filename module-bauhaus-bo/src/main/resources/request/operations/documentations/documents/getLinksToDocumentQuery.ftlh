SELECT
	?text
 WHERE {
	?text insee:additionalMaterial/rdf:rest*/rdf:first ?uri .
	 FILTER(STRENDS(STR(?uri), '${id}'))
	}