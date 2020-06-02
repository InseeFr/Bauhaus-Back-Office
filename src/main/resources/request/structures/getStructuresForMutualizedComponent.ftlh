SELECT DISTINCT ?id ?labelLg1
WHERE {
	GRAPH <${STRUCTURES_COMPONENTS_GRAPH}/graphes/structures> {
        ?structure dcterms:identifier ?id ;
        		   rdfs:label ?labelLg1 ;
                   qb:component ?componentSpecification .

        ?componentSpecification (qb:dimension|qb:measure|qb:attribute) ?component .
        GRAPH <${STRUCTURES_COMPONENTS_GRAPH}/graphes/components> {
			?component dcterms:identifier '${ID}' .
        } .

		FILTER (lang(?labelLg1) = '${LG1}')
	}
}
ORDER BY ?labelLg1