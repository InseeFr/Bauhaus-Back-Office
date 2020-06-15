SELECT DISTINCT ?id ?labelLg1 <#if SEARCH> ?concept </#if> ?type
WHERE {
	GRAPH <${STRUCTURES_COMPONENTS_GRAPH}> {
        ?component dcterms:identifier ?id ;
                   rdf:type ?type ;
                   rdfs:label ?labelLg1 .

        FILTER (lang(?labelLg1) = '${LG1}') .

<#if SEARCH>
        OPTIONAL {
            ?component qb:concept ?conceptObject
        } .

        BIND(STRAFTER(STR(?conceptObject),'/concepts/definition/') AS ?concept) .
</#if>
    }
}
ORDER BY ?labelLg1