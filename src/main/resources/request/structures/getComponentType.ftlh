SELECT DISTINCT ?type
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?component dcterms:identifier '${ID}' ;
    rdf:type ?type .
    FILTER ( ?type = qb:DimensionProperty || ?type = qb:AttributeProperty || ?type = qb:MeasureProperty )
    }