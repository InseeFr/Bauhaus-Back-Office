SELECT DISTINCT ?contributors
FROM <${STRUCTURES_GRAPH}>
WHERE {
    BIND (${URI_STRUCTURE?no_esc} as ?a)
     ?a    dc:contributor ?contributors .
}

