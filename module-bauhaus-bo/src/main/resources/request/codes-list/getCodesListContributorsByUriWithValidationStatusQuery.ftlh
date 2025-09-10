SELECT DISTINCT ?contributors
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    BIND (${URI_CODESLIST?no_esc} as ?a)
     ?a    dc:contributor ?contributors .
}