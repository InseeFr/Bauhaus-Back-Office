SELECT ?territory
FROM<${GEO_SIMS_GRAPH}>
WHERE {
    ?territory skos:prefLabel "${LABEL}"@${LG1}
}