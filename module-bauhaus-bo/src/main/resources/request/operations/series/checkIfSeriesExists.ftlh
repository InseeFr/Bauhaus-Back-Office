SELECT ?uri
FROM <${OPERATIONS_GRAPH}>
WHERE {

<#list URI_SERIES as URI_SERIE>
    {
    <${URI_SERIE}> rdf:type ?type .
    BIND('${URI_SERIE}' AS ?uri) .
    }
    <#sep> UNION </#sep>
</#list>
}