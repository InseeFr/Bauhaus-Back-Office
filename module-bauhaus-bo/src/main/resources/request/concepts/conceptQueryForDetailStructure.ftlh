SELECT ?concept ?labelLg1 ?labelLg2
WHERE {
    GRAPH <${CONCEPTS_GRAPH}> {
        ?concept skos:prefLabel ?labelLg1 .
        FILTER(REGEX(STR(?concept),'/concepts/definition/${ID}')) .
        FILTER (lang(?labelLg1) = '${LG1}') .
        OPTIONAL {
            ?concept skos:prefLabel ?labelLg2 .
            FILTER (lang(?labelLg2) = '${LG2}')
        }
    }
}
