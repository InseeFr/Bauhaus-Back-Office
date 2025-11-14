SELECT DISTINCT ?stamp ?label
FROM <${INSEE_ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_GRAPH}>
WHERE {
    ?organization adms:identifier "${IDENTIFIER}" .
    ?organization adms:identifier ?stamp .
    ?organization skos:prefLabel ?label .
    FILTER (lang(?label) = '${LANG}')
}