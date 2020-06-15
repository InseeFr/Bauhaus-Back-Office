SELECT DISTINCT ?id ?labelLg1
WHERE {
	GRAPH <${STRUCTURES_GRAPH}> {
        ?structure dcterms:identifier ?id ;
        		   rdfs:label ?labelLg1 ;
                   qb:component ?componentSpecification .

        ?componentSpecification (qb:dimension|qb:measure|qb:attribute) ?component .
    GRAPH <${STRUCTURES_COMPONENTS_GRAPH}> {
			?component dcterms:identifier '${ID}' .
        } .

		FILTER (lang(?labelLg1) = '${LG1}')
	}
}
ORDER BY ?labelLg1