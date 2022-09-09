SELECT ?shortLabelLg1 ?shortLabelLg2 ?length
WHERE {
    ?item ?altOrPref ?shortLabel .
    ?item skos:inScheme ?classification .

    FILTER(REGEX(STR(?classification),'/codes/${CLASSIFICATION_ID}/'))
    FILTER(STRENDS(STR(?item),'/${ITEM_ID}'))

    OPTIONAL {
        ?shortLabel skosxl:literalForm ?shortLabelLg1 .
        FILTER (lang(?shortLabelLg1) = '${LG1}')
    }

    ?shortLabel xkos:maxLength ?length .

    OPTIONAL {
        ?shortLabel skosxl:literalForm ?shortLabelLg2 .
        FILTER (lang(?shortLabelLg2) = '${LG2}')
    }
}