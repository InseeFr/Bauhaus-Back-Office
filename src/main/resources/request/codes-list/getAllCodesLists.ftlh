SELECT ?uri ?labelLg1 ?labelLg2 ?range ?id
WHERE {
    GRAPH <${CODES_LISTS_GRAPH}> {
<#if PARTIAL>
    ?uri rdf:type skos:Collection .
<#else>
    ?uri rdf:type skos:ConceptScheme .
</#if>

        ?uri skos:prefLabel ?labelLg1 .
        ?uri skos:notation ?id .
        FILTER(lang(?labelLg1) = '${LG1}') .
        OPTIONAL {?uri skos:prefLabel ?labelLg2 .
        FILTER (lang(?labelLg2) = '${LG2}') } .
        OPTIONAL { ?range rdfs:seeAlso ?uri } .

    }

}