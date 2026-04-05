SELECT DISTINCT ?seriesId ?seriesIri ?seriesLabel ?operationId ?operationIri ?operationLabel
FROM <${GRAPH_URI}>
WHERE {
    ?seriesIri a insee:StatisticalOperationSeries .
    BIND(STRAFTER(STR(?seriesIri), '/operations/serie/') AS ?seriesId) .
    ?seriesIri skos:prefLabel ?seriesLabel .
    FILTER (lang(?seriesLabel) = 'fr')
    ?seriesIri dcterms:hasPart ?operationIri .
    ?operationIri a insee:StatisticalOperation .
    BIND(STRAFTER(STR(?operationIri), '/operations/operation/') AS ?operationId) .
    ?operationIri skos:prefLabel ?operationLabel .
    FILTER (lang(?operationLabel) = 'fr')
}
ORDER BY ?seriesId
LIMIT 10
