SELECT DISTINCT ?iri ?seriesIri ?id ?label ?altLabel
WHERE {
    GRAPH <${OPERATIONS_GRAPH}> {
        ?iri a insee:StatisticalOperation .
        ?iri skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        BIND(STRAFTER(STR(?iri),'/operations/operation/') AS ?id) .

        ?seriesIri dcterms:hasPart ?iri .
        OPTIONAL{
            ?iri skos:altLabel ?altLabel .
            FILTER (lang(?altLabel) = '${LG1}')
        }
    }
}
ORDER BY ?label