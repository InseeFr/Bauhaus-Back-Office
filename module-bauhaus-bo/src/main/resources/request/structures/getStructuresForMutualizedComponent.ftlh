SELECT DISTINCT ?id ?labelLg1 ?validationState
FROM <${STRUCTURES_GRAPH}>
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?structure dcterms:identifier ?id ;
               rdfs:label ?labelLg1 ;
               qb:component ?componentSpecification .

    ?componentSpecification (qb:dimension|qb:measure|qb:attribute) ?component .
    ?component dcterms:identifier '${ID}' .

    OPTIONAL {
        ?component insee:validationState ?validationState
    } .


    FILTER (lang(?labelLg1) = '${LG1}')
}
ORDER BY ?labelLg1