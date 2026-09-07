SELECT ?uri
FROM ${OPERATIONS_GRAPH}
WHERE {

<#list URI_SERIES as URI_SERIE>
    {
    ${URI_SERIE.iri} rdf:type ?type .
    BIND(${URI_SERIE.literal} AS ?uri) .
    }
    <#sep> UNION </#sep>
</#list>
}