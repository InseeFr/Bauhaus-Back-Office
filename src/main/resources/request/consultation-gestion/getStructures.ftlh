SELECT DISTINCT ?id ?dateMiseAJour ?statutValidation
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?structure rdf:type qb:DataStructureDefinition .
    ?structure dcterms:identifier ?id .
    ?structure dcterms:modified ?dateMiseAJour .
    ?structure insee:validationState ?statutValidation .
}

