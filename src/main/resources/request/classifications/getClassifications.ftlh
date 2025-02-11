SELECT DISTINCT ?id ?label (GROUP_CONCAT(DISTINCT ?altLabel; SEPARATOR=", ")  AS ?altLabels)
WHERE {
    GRAPH ?graph {
        ?classification rdf:type skos:ConceptScheme .
        FILTER(regex(str(?classification),'/codes/'))
        ?classification skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        OPTIONAL {
            ?classification skos:altLabel ?altLabel .
            FILTER (lang(?altLabel) = '${LG1}')
        }
        BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id)
    }
    FILTER(REGEX(STR(?graph), '/codes/') )
}
GROUP BY ?id ?label
ORDER BY ?label