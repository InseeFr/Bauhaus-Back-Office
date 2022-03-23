SELECT ?id
FROM <${STRUCTURES_GRAPH}>
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?uriDSD rdf:type qb:DataStructureDefinition .
    ?uriDSD dcterms:identifier "${STRUCTURE_ID}" .
    ?uriDSD qb:component ?uriComponentSpecification .
    ?uriComponentSpecification (qb:measure|qb:dimension|qb:attribute) ?uri .
    ?uri dcterms:identifier ?id .
}