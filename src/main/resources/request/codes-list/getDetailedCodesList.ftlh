SELECT DISTINCT ?id ?labelLg1 ?labelLg2 ?created ?modified ?creator ?contributor ?validationState ?disseminationStatus ?descriptionLg1 ?descriptionLg2
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?list skos:notation '${NOTATION}' ;
    skos:notation ?id ;
    dcterms:created ?created ;
    dcterms:modified ?modified ;
    dc:creator ?creator ;
    dc:contributor ?contributor ;
    insee:validationState ?validationState ;
    insee:disseminationStatus ?disseminationStatus ;
    dc:contributor ?contributor ;
    dcterms:modified ?modified .
    ?list skos:prefLabel ?labelLg1 .
    FILTER(lang(?labelLg1) = '${LG1}') .
    OPTIONAL {?list skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .

    OPTIONAL {?list skos:definition ?descriptionLg1 .
    FILTER(lang(?descriptionLg1) = '${LG1}') } .
    OPTIONAL {?list skos:definition ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .
}
ORDER BY ?labelLg1