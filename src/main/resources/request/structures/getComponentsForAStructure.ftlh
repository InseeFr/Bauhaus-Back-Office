SELECT DISTINCT ?componentDefinitionId ?componentDefinitionCreated ?componentDefinitionModified ?id ?identifiant ?labelLg1 ?labelLg2 ?type ?concept ?codeList ?range ?descriptionLg1 ?descriptionLg2 ?validationState ?order ?created ?modified ?required ?creator ?contributor ?disseminationStatus
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?structure dcterms:identifier "${ID}" ;
               qb:component ?componentSpecification .
    ?componentSpecification (qb:dimension|qb:measure|qb:attribute) ?component ;
                            dcterms:identifier ?componentDefinitionId ;
                            dcterms:created ?componentDefinitionCreated ;
                            dcterms:modified ?componentDefinitionModified .

    OPTIONAL {
        ?componentSpecification qb:componentRequired ?required .
    } .

    OPTIONAL {
        ?componentSpecification qb:order ?order .
    } .

    ?component dcterms:identifier ?id ;
               rdf:type ?type ;
               rdfs:label ?labelLg1 .


    OPTIONAL {
        ?component insee:identifiantMetier ?identifiant .
    } .

    OPTIONAL {
        ?component insee:validationState ?validationState .
    } .

    OPTIONAL {
        ?component dcterms:created ?created
    } .
    OPTIONAL {
        ?component dcterms:modified ?modified
    } .

    OPTIONAL {
        ?component dc:creator ?creator .
    } .

    OPTIONAL {
        ?component dc:contributor ?contributor .
    } .

    OPTIONAL {
        ?component insee:disseminationStatus ?disseminationStatus .
    } .

    OPTIONAL {
        ?component qb:concept ?conceptObject
    } .

    OPTIONAL {
        ?component qb:codeList ?codeList
    } .

    OPTIONAL {
        ?component rdfs:range ?range
    } .
    FILTER (lang(?labelLg1) = '${LG1}')
    OPTIONAL {?component rdfs:label ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .
    OPTIONAL {?component rdfs:comment ?descriptionLg1 .
    FILTER (lang(?descriptionLg1) = '${LG1}') } .
    OPTIONAL {?component rdfs:comment ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .

    BIND(STRAFTER(STR(?conceptObject),'concepts/definition/') AS ?concept) .
}
ORDER BY ?labelLg1