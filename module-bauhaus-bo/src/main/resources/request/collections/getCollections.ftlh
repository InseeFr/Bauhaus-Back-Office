SELECT DISTINCT ?id ?label
WHERE {
    ?collection rdf:type skos:Collection .
    BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) .
    ?collection dcterms:title ?label .
    <#if FILTER_UNPUBLISHED>
        ?collection insee:isValidated 'false'^^xsd:boolean .
    </#if>

    FILTER (lang(?label) = '${LG1}')
}
ORDER BY ?label