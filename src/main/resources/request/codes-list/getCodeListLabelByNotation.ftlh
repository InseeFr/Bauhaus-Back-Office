SELECT ?labelLg1 ?labelLg2
WHERE {
    GRAPH <${CODES_LISTS_GRAPH}> {
        ?codeList rdf:type skos:ConceptScheme .
        ?codeList skos:notation '${NOTATION}' .
        ?codeList skos:prefLabel ?labelLg1 .
        FILTER (lang(?labelLg1) = '${LG1}') .
        ?codeList skos:prefLabel ?labelLg2 .
        FILTER (lang(?labelLg2) = '${LG2}') .
    }
}