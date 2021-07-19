SELECT DISTINCT ?attachment
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?structure dcterms:identifier "${STRUCTURE_ID}" ;
               qb:component ?componentSpecification .
    ?componentSpecification dcterms:identifier "${COMPONENT_SPECIFICATION_ID}" ;
                            qb:componentAttachment ?attachment .
}