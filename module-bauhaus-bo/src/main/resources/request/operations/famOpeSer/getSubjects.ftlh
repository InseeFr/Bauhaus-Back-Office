SELECT  ?id ?labelLg1 ?labelLg2
 FROM <${OPERATIONS_GRAPH}>
WHERE {
?family dcterms:subject ?subjectUri .
?subjectUri skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}') .
?subjectUri skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}') .
?subjectUri skos:notation ?id .
FILTER(STRENDS(STR(?family),'/operations/famille/${ID}')) .
} ORDER BY ?subjectUri
