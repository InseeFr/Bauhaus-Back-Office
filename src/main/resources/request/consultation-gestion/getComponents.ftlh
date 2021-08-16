SELECT ?id ?dateMiseAJour ?statutValidation
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {

    { ?uriComposant rdf:type qb:DimensionProperty . }
    UNION  { ?uriComposant rdf:type qb:MeasureProperty . }
    UNION  { ?uriComposant rdf:type qb:AttributeProperty . }


    ?uriComposant dcterms:identifier ?id .
    ?uriComposant dcterms:modified ?dateMiseAJour .
    ?uriComposant insee:validationState ?statutValidation .
}
