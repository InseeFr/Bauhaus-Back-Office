SELECT ?indicatorId ?id ?labelLg1 ?labelLg2
WHERE {
?indicator a insee:StatisticalIndicator .
?indicator <${LINKPREDICATE}> ?uri .
BIND(STRAFTER(STR(?indicator),'/${PRODUCT_BASE_URI}/') AS ?indicatorId) .
?uri dcterms:identifier  ?id .
?uri skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}') .
OPTIONAL {?uri skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}')} .
}
ORDER BY ?indicatorId ?id
