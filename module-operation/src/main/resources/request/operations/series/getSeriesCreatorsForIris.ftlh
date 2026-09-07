<#-- SERIES_IRIS contient des IRI complètes (<…>), échappées par SparqlLiterals. -->
SELECT ?seriesIri ?creators
FROM ${OPERATIONS_GRAPH}
WHERE {
    VALUES ?seriesIri { <#list SERIES_IRIS as iri>${iri} </#list>}
    {
        ?seriesIri dc:creator ?creators .
    }
    UNION
    {
        ?parent dc:creator ?creators .
        ?parent dcterms:hasPart ?seriesIri .
    }
}
