SELECT ?iri ?labelLg1 ?labelLg2 ?range ?id
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?iri prov:wasDerivedFrom <${IRI}>.
    ?iri skos:prefLabel ?labelLg1 .
    ?iri skos:notation ?id .
    FILTER(lang(?labelLg1) = '${LG1}') .
    OPTIONAL {?iri skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .
}