SELECT ?id ?labelLg1 ?labelLg2
WHERE {
?indicator <${LINKPREDICATE}> ?uri .
?uri dcterms:identifier  ?id .
?uri skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}') .
OPTIONAL {?uri skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}')} .

FILTER(STRENDS(STR(?indicator),'/${PRODUCT_BASE_URI}/${ID}')) .

}
ORDER BY ?id