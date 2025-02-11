SELECT DISTINCT ?id ?label (GROUP_CONCAT(DISTINCT ?altLabel; SEPARATOR=", ") AS ?altLabels)
WHERE {
    GRAPH<${GRAPH}> {
        ?series skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        FILTER(REGEX(STR(?series),'/serieDeNomenclatures/')) .
        OPTIONAL {
            ?series skos:altLabel ?altLabel .
            FILTER (lang(?altLabel) = '${LG1}')
        }
        BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id)
    }
}
GROUP BY ?id ?label
ORDER BY ?label