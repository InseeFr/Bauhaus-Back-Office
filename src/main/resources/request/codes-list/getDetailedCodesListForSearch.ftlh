SELECT DISTINCT ?id ?labelLg1 ?creator ?validationState
FROM <${CODES_LISTS_GRAPH}>
WHERE {
<#if PARTIAL>
    ?list rdf:type skos:Collection .
<#else>
    ?list rdf:type skos:ConceptScheme .
</#if>
    ?list skos:notation ?id ;
    dc:creator ?creator ;
    insee:validationState ?validationState ;
    skos:prefLabel ?labelLg1 .
    FILTER(lang(?labelLg1) = '${LG1}') .
}
ORDER BY ?labelLg1