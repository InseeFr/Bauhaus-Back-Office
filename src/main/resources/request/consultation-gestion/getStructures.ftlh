SELECT DISTINCT ?id ?modified ?validationState
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?structure rdf:type qb:DataStructureDefinition .
    ?structure dcterms:identifier ?id .
    ?structure dcterms:modified ?modified .
    ?structure insee:validationState ?validationState .
}

