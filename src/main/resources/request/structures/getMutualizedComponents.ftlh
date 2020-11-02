SELECT DISTINCT ?id ?identifiant ?labelLg1 ?concept ?type ?codeList
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?component dcterms:identifier ?id ;
               insee:identifiantMetier ?identifiant ;
               rdf:type ?type ;
               rdfs:label ?labelLg1 .


    OPTIONAL {
        ?component qb:codeList ?codeList
    } .

    FILTER (lang(?labelLg1) = '${LG1}') .

    OPTIONAL {
        ?component qb:concept ?conceptObject
    } .

    BIND(STRAFTER(STR(?conceptObject),'/concepts/definition/') AS ?concept) .

}
ORDER BY ?labelLg1