SELECT DISTINCT ?iri ?id ?label ?labelLg2 ?acronym
FROM ${ORGANIZATIONS_GRAPH}
FROM ${ORGANIZATIONS_INSEE_GRAPH}
WHERE {
    ?iri adms:identifier ?id .

    OPTIONAL { ?iri skos:prefLabel ?label .
    FILTER (lang(?label) = ${LG1})}

    OPTIONAL {
        ?iri skos:prefLabel ?label2 .
        FILTER (lang(?label2) = ${LG2})
    } .

    OPTIONAL { ?iri vaem:acronym ?acronym . }

    BIND(if(bound(?label2), ?label2, ?label) AS ?labelLg2)
}
GROUP BY ?iri ?id ?label ?labelLg2 ?acronym
ORDER BY ?label