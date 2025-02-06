SELECT DISTINCT ?id ?label ?altLabels
WHERE {
    GRAPH<${GRAPH}> {
        ?series skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        FILTER(REGEX(STR(?series),'/serieDeNomenclatures/')) .
        OPTIONAL {
            ?series skos:altLabel ?altLabels .
            FILTER (lang(?altLabels) = '${LG1}')
        }
        BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id)
    }
}
ORDER BY ?label