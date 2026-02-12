SELECT ?uri
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
    {
        ?uri adms:identifier '${IDENTIFIER}' .
    } UNION {
        ?uri dct:identifier '${IDENTIFIER}' .
    }
}
