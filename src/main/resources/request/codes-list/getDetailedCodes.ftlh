SELECT ?labelLg1 ?labelLg2 ?code ?descriptionLg1 ?descriptionLg2 ?parents
FROM <${CODES_LISTS_GRAPH}>
WHERE {
<#if PARTIAL>
    ?list skos:member ?codeUri .
<#else>
    ?codeUri skos:inScheme ?list .
</#if>
    ?list skos:notation '${NOTATION}' .
    ?codeUri skos:notation ?code .
    ?codeUri skos:prefLabel ?labelLg1 .
    FILTER(lang(?labelLg1) = '${LG1}') .
    OPTIONAL {?codeUri skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .


    OPTIONAL {
    ?codeUri skos:broader ?parentsParent .
    ?parentsParent skos:notation ?parents
    } .

    OPTIONAL {?codeUri skos:definition ?descriptionLg1 .
    FILTER(lang(?descriptionLg1) = '${LG1}') } .
    OPTIONAL {?codeUri skos:definition ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .
}
ORDER BY ?labelLg1