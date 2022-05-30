SELECT ?uri ?id ?notation ?prefLabelLg1 ?prefLabelLg2 ?dateCreation ?dateMiseAJour ?dateFinValidité ?statutValidation ?version ?idRelation ?necessairePour ?idParent ?uriParent ?idParentRelation
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?uri rdf:type qb:DataStructureDefinition .
    ?uri dcterms:identifier "${STRUCTURE_ID}" .
    BIND("${STRUCTURE_ID}" AS ?id) .
    OPTIONAL { ?uri skos:notation ?notation . }
    ?uri rdfs:label ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    ?uri rdfs:label ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .
    ?uri dcterms:created ?dateCreation .
    ?uri dcterms:modified ?dateMiseAJour .
    OPTIONAL {?uri dcterms:valid ?dateFinValidité . }
    OPTIONAL { ?uri insee:validationState ?statutValidation . }
    OPTIONAL {?uri pav:version ?version . }
    OPTIONAL { ?uri dcterms:relation ?idRelation }
    OPTIONAL { ?uri dcterms:isRequiredBy ?necessairePour }
    OPTIONAL {
        ?uri prov:wasDerivedFrom ?idParent .
        ?uriParent dcterms:identifier ?idParent

    }
    OPTIONAL {
        ?uriParent dcterms:relation ?idParentRelation .
    }
}