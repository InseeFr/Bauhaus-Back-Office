SELECT DISTINCT ?notation ?modified ?validationState
FROM <${CODELIST_GRAPH}>
WHERE {
    ?listeCode rdf:type skos:ConceptScheme .
    ?listeCode skos:notation ?notation .
    OPTIONAL {?listeCode dcterms:modified ?modified . }
    OPTIONAL {?listeCode insee:validationState ?validationState . }
}

