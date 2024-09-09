SELECT ?id ?typeOfObject ?labelLg1 ?labelLg2
WHERE {
?indic <${LINKPREDICATE}> ?uriLinked .
?uriLinked skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}') .
OPTIONAL {?uriLinked skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}')} .
?uriLinked rdf:type ?typeOfObject .
BIND(REPLACE(STR(?uriLinked), '^.*/([^/]+)$', '$1') AS ?id) .

FILTER(STRENDS(STR(?indic),'/${PRODUCT_BASE_URI}/${ID}')) .

}
ORDER BY ?labelLg1