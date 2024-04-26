SELECT DISTINCT ?id ?label
WHERE {
    GRAPH <${GRAPH}> {
        ?families skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        FILTER(REGEX(STR(?families),'/familleDeNomenclatures/')) .
        BIND(STRAFTER(STR(?families),'/codes/familleDeNomenclatures/') AS ?id)
    }
}
ORDER BY ?label