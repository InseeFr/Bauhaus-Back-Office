SELECT DISTINCT ?id ?identifiant ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?created ?modified ?creator ?contributor ?disseminationStatus ?validationState
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?structure dcterms:identifier "${ID}" ;
               skos:notation ?identifiant ;
               rdfs:label ?labelLg1 ;
               rdfs:label ?labelLg2 .
    FILTER (lang(?labelLg1) = '${LG1}')
    FILTER (lang(?labelLg2) = '${LG2}')

    OPTIONAL {
        ?structure insee:disseminationStatus ?disseminationStatus .
    } .

    OPTIONAL {
        ?structure dcterms:created ?created .
    } .

    OPTIONAL {
        ?structure dcterms:modified ?modified .
    } .

    OPTIONAL {
        ?structure dc:creator ?creator .
    } .

    OPTIONAL {
        ?structure insee:validationState ?validationState
    } .

    OPTIONAL {
        ?structure rdfs:comment ?descriptionLg1 .
        FILTER (lang(?descriptionLg1) = '${LG1}')
    } .
    OPTIONAL {
        ?structure rdfs:comment ?descriptionLg2
        FILTER (lang(?descriptionLg2) = '${LG2}')
    } .
}
ORDER BY ?labelLg1
