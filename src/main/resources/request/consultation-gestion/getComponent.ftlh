SELECT ?id (?uriComposant as ?uri) ?type ?notation
    ?dateMiseAJour ?statutValidation ?prefLabelLg1 ?prefLabelLg2
    ?uriConcept ?idConcept ?representation
    ?uriListeCode ?idListeCode ?version ?uriComponentParentId ?uriComponentParentNotation ?dateCreation
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${CODELIST_GRAPH}>
WHERE {
    ?uriComposant dcterms:identifier "${ID}" .
    BIND("${ID}" AS ?id) .

    {
        ?uriComposant rdf:type qb:DimensionProperty .
        BIND("Dimension" as ?type) .
    }
    UNION  {
        ?uriComposant rdf:type qb:MeasureProperty .
        BIND("Mesure" as ?type) .
    }
    UNION  {
       ?uriComposant rdf:type qb:AttributeProperty .
        BIND("Attribut" as ?type) .
    }

    ?uriComposant skos:notation ?notation .
    ?uriComposant dcterms:modified ?dateMiseAJour .
    ?uriComposant insee:validationState ?statutValidation .

    ?uriComposant rdfs:label ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .

    ?uriComposant rdfs:label ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .

    OPTIONAL {
        ?uriComposant skos:broader ?uriComponentParent .
        ?uriComponentParent dcterms:identifier ?uriComponentParentId .
        ?uriComponentParent skos:notation ?uriComponentParentNotation .
    }

    OPTIONAL {
        ?uriComposant dcterms:created ?dateCreation .
    }
    OPTIONAL {
        ?uriComposant qb:concept ?uriConcept .
        BIND(STRAFTER(STR(?uriConcept), "${CONCEPTS_BASE_URI}/") AS ?idConcept)
    }
    OPTIONAL {
        ?uriComposant rdfs:range ?representation .
    }
    OPTIONAL {
        ?uriComposant qb:codeList ?uriListeCode .
        ?uriListeCode skos:notation ?idListeCode .
    }
    OPTIONAL {
        ?uriComposant pav:version ?version .
    }
}