SELECT DISTINCT ?stamp ?label
FROM <${ORGANIZATIONS_GRAPH}>
WHERE {
    VALUES ?stamp {
        <#list IDENTIFIERS as identifier>
        "${identifier}"
        </#list>
    }
    ?organization adms:identifier ?stamp .
    ?organization skos:prefLabel ?label .
    FILTER (lang(?label) = '${LANG}')
}