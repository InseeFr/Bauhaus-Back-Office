SELECT ?seriesIri ?creators
FROM <${OPERATIONS_GRAPH}>
WHERE {
    VALUES ?seriesIri { <#list SERIES_IRIS as iri><${iri?no_esc}> </#list>}
    {
        ?seriesIri dc:creator ?creators .
    }
    UNION
    {
        ?parent dc:creator ?creators .
        ?parent dcterms:hasPart ?seriesIri .
    }
}
