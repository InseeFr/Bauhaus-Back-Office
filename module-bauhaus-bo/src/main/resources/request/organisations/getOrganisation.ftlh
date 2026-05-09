SELECT DISTINCT ?stamp ?label
FROM <${ORGANIZATIONS_GRAPH}>
WHERE {
    ?organization adms:identifier "${IDENTIFIER}" .
    ?organization adms:identifier ?stamp .
    ?organization skos:prefLabel ?label .
    FILTER (lang(?label) = '${LANG}')
}