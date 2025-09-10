SELECT ?code ?linkCode ?linkType
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?uri skos:notation "${NOTATION}" .
    ?codeUri skos:inScheme ?uri .
    ?codeUri skos:notation ?code .

    {
        ?codeUri  skos:narrower ?narrowerUri .
        ?narrowerUri skos:notation ?linkCode .
        BIND("narrower" AS ?linkType) .
    }
    UNION
    {
        ?codeUri  skos:broader ?broaderUri .
        ?broaderUri skos:notation ?linkCode .
        BIND("broader" AS ?linkType) .
    }
    UNION
    {
        ?codeUri  skos:closeMatch ?broaderUri .
        ?broaderUri skos:notation ?linkCode .
        BIND("closeMatch" AS ?linkType) .
    }

}