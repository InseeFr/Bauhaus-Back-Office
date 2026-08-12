SELECT DISTINCT ?item ?id ?labelLg1 ?labelLg2
WHERE {
?level rdf:type xkos:ClassificationLevel .
FILTER(STRENDS(STR(?level),'/codes/${CLASSIFICATION_ID}/${LEVEL_ID}'))
?level skos:member ?item .
?item skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}')
OPTIONAL {?item skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}') } .
?item skos:notation ?id .
}
ORDER BY ?id
