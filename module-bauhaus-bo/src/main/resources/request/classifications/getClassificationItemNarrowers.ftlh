SELECT ?id ?labelLg1 ?labelLg2
WHERE {
    ?item skos:narrower ?narrower .
    ?item skos:inScheme ?classification .
    ?narrower skos:inScheme ?classificationNarrower .
    FILTER(REGEX(STR(?classification),'/codes/${CLASSIFICATION_ID}/'))
    FILTER(REGEX(STR(?classificationNarrower),'/codes/${CLASSIFICATION_ID}/'))
    FILTER(STRENDS(STR(?item),'/${ITEM_ID}'))
    ?narrower skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')
    ?narrower skos:notation ?id .
    OPTIONAL {
        ?narrower skos:prefLabel ?labelLg2 .
        FILTER (lang(?labelLg2) = '${LG2}')
    }
}
ORDER BY ?id