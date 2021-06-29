SELECT ?uri ?uriNiveau ?idNiveau
FROM <${CODELIST_GRAPH}>
WHERE {
    ?uriListe rdf:type skos:ConceptScheme .
    ?uriListe skos:notation "${NOTATION}" .
    ?uri rdf:type skos:Concept .
    ?uri skos:inScheme ?uriListe .
    ?uriListe xkos:levels/(rdf:rest)*/rdf:first ?uriNiveau .
    ?uriNiveau rdf:type xkos:ClassificationLevel .
    ?uriNiveau skos:member ?uri .
    ?uriNiveau skos:notation ?idNiveau .
}
order by ?uriCode