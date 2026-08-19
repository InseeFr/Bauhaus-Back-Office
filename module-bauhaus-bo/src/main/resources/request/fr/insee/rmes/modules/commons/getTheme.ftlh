SELECT ?uri ?label
FROM ${THEMES_GRAPH}
{
    ?uri a ${THEME_TYPE} ;
        skos:prefLabel ?label .

    FILTER (lang(?label) = ${LG1}) .
} ORDER BY ASC(?label)
