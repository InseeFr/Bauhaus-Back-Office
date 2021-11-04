SELECT ?id ?notation
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${CODELIST_GRAPH}>
WHERE {
    ?uriComposant dcterms:identifier "${ID}" ;
        			  skos:narrower ?uriComponentChild .
    ?uriComponentChild dcterms:identifier ?id .
    ?uriComponentChild skos:notation ?notation .
}