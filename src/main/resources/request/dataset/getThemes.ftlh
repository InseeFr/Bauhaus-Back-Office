SELECT ?uri ?label
FROM <${CONCEPTS_GRAPH}>
{
	?uri a <${THEME_TYPE}> .
    ?uri skos:prefLabel ?label .
} ORDER BY ASC(?label)