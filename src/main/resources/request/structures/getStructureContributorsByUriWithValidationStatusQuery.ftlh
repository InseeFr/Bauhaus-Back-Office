SELECT DISTINCT?contributors
FROM <${STRUCTURES_GRAPH}>
WHERE {
    ?s ?p ?o .
    ?s dc:contributor ?contributors .
FILTER CONTAINS(str(?s), '${URI_STRUCTURE?no_esc}')
}

