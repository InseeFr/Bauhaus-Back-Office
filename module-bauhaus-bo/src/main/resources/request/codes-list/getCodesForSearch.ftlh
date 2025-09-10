SELECT ?labelLg1 ?code ?id
FROM <${CODES_LISTS_GRAPH}>
WHERE {
<#if PARTIAL>
    ?list skos:member ?codeUri .
<#else>
    ?codeUri skos:inScheme ?list .
</#if>
    ?list skos:notation ?id .
    ?codeUri skos:notation ?code .
    ?codeUri skos:prefLabel ?labelLg1 .
    FILTER(lang(?labelLg1) = '${LG1}') .
}
ORDER BY ?labelLg1