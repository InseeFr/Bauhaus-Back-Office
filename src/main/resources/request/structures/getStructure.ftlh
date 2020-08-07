SELECT DISTINCT ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?created ?modified
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?structure dcterms:identifier "${ID}" ;
               rdfs:label ?labelLg1 ;
               rdfs:label ?labelLg2 .
    OPTIONAL {
        ?structure dcterms:created ?created .
    } .

    OPTIONAL {
        ?structure dcterms:modified ?modified .
    } .

    OPTIONAL {
        ?structure rdfs:label ?labelLg2 .
        FILTER (lang(?labelLg2) = '${LG2}')
    } .
    OPTIONAL {
        ?structure rdfs:comment ?descriptionLg1 .
        FILTER (lang(?descriptionLg1) = '${LG1}')
    } .
    OPTIONAL {
        ?structure rdfs:comment ?descriptionLg2
        FILTER (lang(?descriptionLg2) = '${LG2}')
    } .
    FILTER (lang(?labelLg1) = '${LG1}')
}
ORDER BY ?labelLg1