SELECT DISTINCT ?id ?labelLg1 ?creator ?validationState
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?component dcterms:identifier ?id ;
               rdfs:label ?labelLg1 .
    OPTIONAL {
        ?component dc:creator ?creator .
    } .

    OPTIONAL {
        ?component insee:validationState ?validationState
    } .

    FILTER (lang(?labelLg1) = '${LG1}') .
}
ORDER BY ?labelLg1