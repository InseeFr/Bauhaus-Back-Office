SELECT DISTINCT ?id ?identifier ?labelLg1 ?labelLg2 ?altLabel
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
    ?organization adms:identifier ?id .
    ?organization dcterms:identifier ?identifier .

    OPTIONAL { ?organization skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')}
    OPTIONAL { ?organization skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}')}
    OPTIONAL {?organization skos:altLabel ?altLabel .}
}
GROUP BY ?id ?identifier ?labelLg1 ?labelLg2 ?altLabel
ORDER BY ?labelLg1