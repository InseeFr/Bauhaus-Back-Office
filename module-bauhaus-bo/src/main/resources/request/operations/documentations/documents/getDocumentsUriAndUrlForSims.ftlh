SELECT ?id ?uri ?url
FROM ${DOCUMENTATION_GRAPH_IRI}
FROM ${DOCUMENTS_GRAPH}
WHERE {
    ?sims insee:additionalMaterial/rdf:rest*/rdf:first ?uri .
    ?uri <http://schema.org/url> ?url .
    FILTER(REGEX(STR(?uri), '/document/'))
    BIND(REPLACE( STR(?uri) , '(.*/)(\\w.*$)', '$2' ) AS ?id) .
}