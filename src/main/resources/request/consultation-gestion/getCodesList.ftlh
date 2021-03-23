SELECT ?uri ?notation ?prefLabelLg1 ?prefLabelLg2 ?dateCréation ?dateMiseAJour ?dateFinValidité ?statutValidation
FROM <${CODELIST_GRAPH}>
WHERE {
    ?uri rdf:type skos:ConceptScheme .
    ?uri skos:notation "${NOTATION}" .
    BIND("CL_FREQ" AS ?notation) .
    ?uri skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    ?uri skos:prefLabel ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .
    OPTIONAL {?listeCode dcterms:created ?dateCréation . }
    OPTIONAL {?listeCode dcterms:modified ?dateMiseAJour . }
    OPTIONAL {?listeCode dcterms:valid ?dateFinValidité . }
    OPTIONAL {?listeCode insee:validationState ?statutValidation . }
}