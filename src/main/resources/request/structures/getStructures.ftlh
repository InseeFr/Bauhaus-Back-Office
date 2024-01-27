SELECT DISTINCT ?iri ?id ?labelLg1 ?creator ?validationState
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?iri dcterms:identifier ?id ;
               rdfs:label ?labelLg1 .
    OPTIONAL {
        ?iri dc:creator ?creator .
    } .

    OPTIONAL {
        ?iri insee:validationState ?validationState
    } .

    FILTER (lang(?labelLg1) = '${LG1}') .
}
ORDER BY ?labelLg1