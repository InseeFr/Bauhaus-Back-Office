SELECT ?labelLg1 ?labelLg2 ?code ?descriptionLg1 ?descriptionLg2 ?parents ?codeUri ?lastCodeUriSegment
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

    BIND(STRAFTER(STR(?codeUri),'${CODE_LIST_BASE_URI}/') AS ?lastCodeUriSegmentWithId) .
    BIND(STRBEFORE(?lastCodeUriSegmentWithId, CONCAT('/', ?code)) AS ?lastCodeUriSegment) .
    OPTIONAL {?codeUri skos:definition ?descriptionLg1 .
    FILTER(lang(?descriptionLg1) = '${LG1}') } .
    OPTIONAL {?codeUri skos:definition ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .

    <#if SEARCH_CODE??>
        FILTER (contains(lcase(str(?code)),lcase("${SEARCH_CODE}")))
    </#if>
    <#if SEARCH_LABEL_LG1??>
        FILTER (contains(lcase(str(?labelLg1)),lcase("${SEARCH_LABEL_LG1}")))
    </#if>
}
ORDER BY ?${SORT}
<#if PER_PAGE??>
LIMIT ${PER_PAGE} OFFSET ${OFFSET}
</#if>
