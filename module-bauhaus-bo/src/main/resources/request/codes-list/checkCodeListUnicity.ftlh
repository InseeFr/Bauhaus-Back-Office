ASK
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    {
        ?iri skos:notation "${ID}"
    }
    UNION
    {
        <${IRI}> rdf:type skos:ConceptScheme
    }
    UNION
    {
        <${IRI}> rdf:type skos:Collection
    }
<#if !PARTIAL>
    UNION
    {
        ?iri rdf:type skos:ConceptScheme .
        ?iri rdfs:seeAlso <${SEE_ALSO}>
    }
</#if>
}