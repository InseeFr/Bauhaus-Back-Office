SELECT DISTINCT ?id ?labelLg1 ?labelLg2
WHERE {
    ?level rdf:type xkos:ClassificationLevel .
    FILTER(REGEX(STR(?level),${CLASSIFICATION_URI_PATTERN}))
    BIND(STRAFTER(STR(?level),${CLASSIFICATION_ID_SEGMENT}) AS ?id)
    ?level skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = ${LG1})
    OPTIONAL {
        ?level skos:prefLabel ?labelLg2 .
        FILTER (lang(?labelLg2) = ${LG2})
    }
    ?level xkos:depth ?depth .
}
ORDER BY ?depth