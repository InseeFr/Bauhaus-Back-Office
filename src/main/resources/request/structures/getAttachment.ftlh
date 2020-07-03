SELECT DISTINCT ?attachment
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?structure dcterms:identifier "${COMPONENT_SPECIFICATION_ID}" ;
    		qb:componentAttachment ?attachment .

}