SELECT DISTINCT ?iri ?seriesIri ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel)
WHERE {
    GRAPH <${OPERATIONS_GRAPH}> {
        ?iri a insee:StatisticalOperation .
        ?iri skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
        BIND(STRAFTER(STR(?iri),'/operations/operation/') AS ?id) .

        ?seriesIri dcterms:hasPart ?iri .
        OPTIONAL{
            ?iri skos:altLabel ?altLabelLg1 .
            FILTER (lang(?altLabelLg1) = '${LG2}')
        }
    }
}
GROUP BY ?iri ?seriesIri ?id ?label
ORDER BY ?label