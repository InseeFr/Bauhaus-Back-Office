SELECT ?code ?labelLg1 ?labelLg2
FROM <${CODES_LISTS_GRAPH}>
WHERE {
?codeList skos:inScheme <${CODE_LIST}> .
?codeList skos:notation ?code .
?codeList skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}')
OPTIONAL {?codeList skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}') } .
}