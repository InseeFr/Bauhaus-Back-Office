SELECT ?uri ?id
WHERE {
    ?uri rdf:type skos:ConceptScheme
<#list IDS as ID>
    {
    FILTER(REGEX(STR(?uri),'/codes/${ID}/')) .
    }
    <#sep> UNION </#sep>
</#list>
    BIND(STRBEFORE(STRAFTER(STR(?uri),'/codes/'), '/') AS ?id)
}