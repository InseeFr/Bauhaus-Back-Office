SELECT DISTINCT ?notation ?dateMiseAJour ?statutValidation
FROM <${CODELIST_GRAPH}>
WHERE {
    ?listeCode rdf:type skos:ConceptScheme .
    ?listeCode skos:notation ?notation .
    OPTIONAL {?listeCode dcterms:modified ?dateMiseAJour . }
    OPTIONAL {?listeCode insee:validationState ?statutValidation . }
}

