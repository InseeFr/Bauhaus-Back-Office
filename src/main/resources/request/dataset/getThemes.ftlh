SELECT ?uri ?label
FROM <${CONCEPTS_GRAPH}>
{
	?uri a <${THEME_TYPE}> .
    ?uri skos:prefLabel ?label .
    FILTER (lang(?label) = '${LG1}') .
} ORDER BY ASC(?label)