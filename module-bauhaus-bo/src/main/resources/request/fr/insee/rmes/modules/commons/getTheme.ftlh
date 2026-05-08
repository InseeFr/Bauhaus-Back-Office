SELECT ?uri ?label
FROM <${CONCEPTS_GRAPH}>
{
    ?uri skos:prefLabel ?label .
    OPTIONAL {
        ?uri skos:inScheme ?uriConceptScheme
    }

    FILTER (lang(?label) = '${LG1}') .
} ORDER BY ASC(?label)