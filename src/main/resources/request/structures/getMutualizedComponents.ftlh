SELECT DISTINCT ?id ?identifiant ?labelLg1 ?concept ?type ?codeList ?validationState ?creator ?range
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?component dcterms:identifier ?id ;
               skos:notation ?identifiant ;
               rdf:type ?type ;
               rdfs:label ?labelLg1 .

    filter ( ?type = qb:DimensionProperty || ?type = qb:AttributeProperty || ?type = qb:MeasureProperty )
    OPTIONAL {
        ?component dc:creator ?creator .
    } .

    OPTIONAL {
        ?component insee:validationState ?validationState
    } .

    OPTIONAL {
        ?component qb:codeList ?codeList
    } .

    FILTER (lang(?labelLg1) = '${LG1}') .

    OPTIONAL {
        ?component qb:concept ?conceptObject
    } .

    OPTIONAL {
        ?component rdfs:range ?range
    } .

    BIND(STRAFTER(STR(?conceptObject),'/concepts/definition/') AS ?concept) .

}
ORDER BY ?labelLg1