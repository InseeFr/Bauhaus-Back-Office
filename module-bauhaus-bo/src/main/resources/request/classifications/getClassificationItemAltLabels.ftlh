SELECT ?shortLabelUri ?shortLabelLg1 ?shortLabelLg2 ?length
WHERE {
    ?item ?altOrPref ?shortLabelUri .
    ?item skos:inScheme ?classification .

    FILTER(REGEX(STR(?classification),${CLASSIFICATION_URI_PATTERN}))
    FILTER(STRENDS(STR(?item),${ITEM_URI_SUFFIX}))

    ?shortLabelUri skosxl:literalForm ?shortLabelLg1 .
    FILTER (lang(?shortLabelLg1) = ${LG1})

    ?shortLabelUri xkos:maxLength ?length .

    OPTIONAL {
        ?shortLabelUri skosxl:literalForm ?shortLabelLg2 .
        FILTER (lang(?shortLabelLg2) = ${LG2})
    }
}