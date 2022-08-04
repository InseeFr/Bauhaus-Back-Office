SELECT ?code ?labelLg1 ?labelLg2 ?iri
WHERE {
    GRAPH <${CODES_LISTS_GRAPH}> {
        ?codeList rdf:type skos:ConceptScheme .
        ?codeList skos:notation '${NOTATION}' .
        ?iri skos:inScheme ?codeList .
        ?iri skos:notation ?code .
        ?iri skos:prefLabel ?labelLg1 .
        FILTER (lang(?labelLg1) = '${LG1}') .
        ?iri skos:prefLabel ?labelLg2 .
        FILTER (lang(?labelLg2) = '${LG2}') .
    }
}