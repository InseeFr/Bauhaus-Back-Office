SELECT DISTINCT ?id ?labelLg1 ?labelLg2
WHERE {
?classification xkos:belongsTo ?series .
?classification skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = ${LG1})
OPTIONAL {?classification skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = ${LG2}) }
FILTER(REGEX(STR(?series),${SERIES_URI_PATTERN})) .
BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id)
}
ORDER BY ?labelLg1
