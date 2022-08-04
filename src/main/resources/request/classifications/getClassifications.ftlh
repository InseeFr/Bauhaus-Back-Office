SELECT DISTINCT ?id ?label
WHERE {
    GRAPH ?graph {
        ?classification rdf:type skos:ConceptScheme .
        FILTER(regex(str(?classification),'/codes/'))
        ?classification skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id)
    }
    FILTER(REGEX(STR(?graph), '/codes/') )
}
ORDER BY ?label