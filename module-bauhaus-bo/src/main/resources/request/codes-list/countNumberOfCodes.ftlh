SELECT (COUNT(?iri) AS ?count)
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

        <#if SEARCH_CODE??>
            FILTER (contains(lcase(str(?code)),lcase("${SEARCH_CODE}")))
        </#if>
        <#if SEARCH_LABEL_LG1??>
            FILTER (contains(lcase(str(?labelLg1)),lcase("${SEARCH_LABEL_LG1}")))
        </#if>
    }
}