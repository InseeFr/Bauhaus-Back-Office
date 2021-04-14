SELECT ?uri ?labelLg1 ?labelLg2 ?range ?notation
WHERE {
    {
        GRAPH <${CODES_LISTS_GRAPH}> {
            ?uri rdf:type skos:ConceptScheme .
            ?uri skos:prefLabel ?labelLg1 .
            ?uri skos:notation ?notation .
            FILTER(lang(?labelLg1) = '${LG1}') .
            OPTIONAL {?uri skos:prefLabel ?labelLg2 .
            FILTER (lang(?labelLg2) = '${LG2}') } .
            ?range rdfs:seeAlso ?uri
        }
    }
    UNION
    {
        ?uri rdf:type xkos:ClassificationLevel .
        ?uri skos:prefLabel ?labelLg1 .
        FILTER(lang(?labelLg1) = '${LG1}') .
        OPTIONAL {?uri skos:prefLabel ?labelLg2 .
        FILTER (lang(?labelLg2) = '${LG2}') } .
        ?uri xkos:organizedBy ?range
    }
}