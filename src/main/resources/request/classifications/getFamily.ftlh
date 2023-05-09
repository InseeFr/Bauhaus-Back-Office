SELECT ?prefLabelLg1
WHERE {
    GRAPH<${GRAPH}> {
        ?family skos:prefLabel ?prefLabelLg1 .
        FILTER (lang(?prefLabelLg1) = '${LG1}')
        FILTER(REGEX(STR(?family),'/familleDeNomenclatures/${ID}'))
    }
}