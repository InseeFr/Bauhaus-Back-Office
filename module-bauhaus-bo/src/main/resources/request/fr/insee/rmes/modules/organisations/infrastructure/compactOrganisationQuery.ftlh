SELECT ?iri ?identifier ?label
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
    <#if IDENTIFIER??>
    ?iri adms:identifier '${IDENTIFIER}' .
    </#if>
    <#if IDENTIFIERS??>
    FILTER(?identifier IN (<#list IDENTIFIERS as id>'${id}'<#sep>, </#sep></#list>))
    </#if>
    ?iri adms:identifier ?identifier .
    ?iri skos:prefLabel ?label .
}