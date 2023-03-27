SELECT DISTINCT ?id ?labelLg1 ?labelLg2
WHERE {
    ?series xkos:belongsTo ?family .
    ?series skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')
    OPTIONAL {?series skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') }
    FILTER(REGEX(STR(?family),'/familleDeNomenclatures/${ID}')) .
    BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id)
}
ORDER BY ?labelLg1