SELECT ?uri ?id
WHERE {
    ?uri rdf:type skos:ConceptScheme .
    BIND(STRBEFORE(STRAFTER(STR(?uri),'/codes/'), '/') AS ?id)
    FILTER(?id IN (<#list IDS as ID>'${ID}'<#sep>, </#sep></#list>))
}
