SELECT DISTINCT ?id ?labelLg1 ?concept ?type ?codeList
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?component dcterms:identifier ?id ;
               rdf:type ?type ;
               rdfs:label ?labelLg1 .


    OPTIONAL {
        ?component qb:codeList ?codeList
    } .

#    { ?component insee:validationState 'Modified' }
#    UNION
#    { ?component insee:validationState 'Published' }

    FILTER (lang(?labelLg1) = '${LG1}') .

    OPTIONAL {
        ?component qb:concept ?conceptObject
    } .

    BIND(STRAFTER(STR(?conceptObject),'/concepts/definition/') AS ?concept) .

}
ORDER BY ?labelLg1