SELECT DISTINCT ?id ?labelLg1
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?component dcterms:identifier ?id ;
               rdfs:label ?labelLg1 .

    FILTER (lang(?labelLg1) = '${LG1}') .
}
ORDER BY ?labelLg1