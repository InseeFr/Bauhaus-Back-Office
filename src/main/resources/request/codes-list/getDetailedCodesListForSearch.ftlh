SELECT DISTINCT ?id ?labelLg1 ?creator ?validationState
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?list skos:notation ?id ;
    dc:creator ?creator ;
    insee:validationState ?validationState ;
    skos:prefLabel ?labelLg1 .
    FILTER(lang(?labelLg1) = '${LG1}') .
}
ORDER BY ?labelLg1