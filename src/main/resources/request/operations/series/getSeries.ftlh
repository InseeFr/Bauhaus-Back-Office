SELECT ?id ?labelLg1 ?labelLg2
WHERE { GRAPH <${OPERATIONS_GRAPH}> {
FILTER(STRENDS(STR(?operation),'/operations/operation/${ID}')) .
?seriesUri dcterms:hasPart ?operation .
?seriesUri skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}') .
?seriesUri skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}') .
BIND(STRAFTER(STR(?seriesUri),'/serie/') AS ?id) .

}}
LIMIT 1