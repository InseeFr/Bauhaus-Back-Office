SELECT ?uri ?url
FROM <${DOCUMENTATIONS_GRAPH}/${id}>
FROM <${DOCUMENTS_GRAPH}>
WHERE {
    ?sims insee:additionalMaterial ?uri .
    ?uri <http://schema.org/url> ?url .
    FILTER(REGEX(STR(?uri), '/document/'))
}