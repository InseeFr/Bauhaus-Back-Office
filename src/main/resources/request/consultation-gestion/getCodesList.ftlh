SELECT ?uri ?notation ?prefLabelLg1 ?prefLabelLg2 ?dateCréation ?dateMiseAJour ?dateFinValidité ?statutValidation
FROM <${CODELIST_GRAPH}>
WHERE {
    ?uri rdf:type skos:ConceptScheme .
    ?uri skos:notation "${NOTATION}" .
    BIND("${NOTATION}" AS ?notation) .
    ?uri skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    ?uri skos:prefLabel ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .
    OPTIONAL {?uri dcterms:created ?dateCréation . }
    OPTIONAL {?uri dcterms:modified ?dateMiseAJour . }
    OPTIONAL {?uri dcterms:valid ?dateFinValidité . }
    OPTIONAL {?uri insee:validationState ?statutValidation . }
}