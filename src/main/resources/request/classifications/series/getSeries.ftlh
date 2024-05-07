SELECT DISTINCT ?id ?label ?altLabel
WHERE {
    GRAPH<${GRAPH}> {
        ?series skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        FILTER(REGEX(STR(?series),'/serieDeNomenclatures/')) .
        OPTIONAL {
            ?families skos:altLabel ?altLabel .
        }
        BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id)
    }
}
ORDER BY ?label