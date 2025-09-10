SELECT DISTINCT ?iri ?id ?identifiant ?labelLg1 ?concept ?type ?codeList ?validationState ?creator ?range
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?iri dcterms:identifier ?id ;
               skos:notation ?identifiant ;
               rdf:type ?type ;
               rdfs:label ?labelLg1 .

    FILTER (?type IN (${TYPES} ) )
    OPTIONAL {
        ?iri dc:creator ?creator .
    } .

    OPTIONAL {
        ?iri insee:validationState ?validationState
    } .

    OPTIONAL {
        ?iri qb:codeList ?codeList
    } .

    FILTER (lang(?labelLg1) = '${LG1}') .

    OPTIONAL {
        ?iri qb:concept ?conceptObject
    } .

    OPTIONAL {
        ?iri rdfs:range ?range
    } .

    BIND(STRAFTER(STR(?conceptObject),'/concepts/definition/') AS ?concept) .

}
ORDER BY ?labelLg1