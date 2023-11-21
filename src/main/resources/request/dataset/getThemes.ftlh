SELECT ?uri ?label ?idConceptScheme
FROM <${CONCEPTS_GRAPH}>
{
    ?uri skos:prefLabel ?label .
    OPTIONAL {
        ?uri skos:inScheme ?uriConceptScheme .
        ?uriConceptScheme skos:notation ?idConceptScheme
    }
    FILTER (CONTAINS('${CONCEPT_SCHEME_FILTER}', ?idConceptScheme)) .
    FILTER (lang(?label) = '${LG1}') .
} ORDER BY ASC(?label)