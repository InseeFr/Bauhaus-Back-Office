SELECT DISTINCT ?id ?labelLg1 ?labelLg2
WHERE {
?operation a insee:StatisticalOperation .
?series dcterms:hasPart ?operation
FILTER(STRENDS(STR(?series),'/operations/serie/${ID}')) .
?operation skos:prefLabel ?labelLg1 .
FILTER (lang(?labelLg1) = '${LG1}')
?operation skos:prefLabel ?labelLg2 .
FILTER (lang(?labelLg2) = '${LG2}')

BIND(STRAFTER(STR(?operation),'/operations/operation/') AS ?id) .
FILTER NOT EXISTS { ?documentation sdmx-mm:target ?operation }

}
GROUP BY ?id ?labelLg1 ?labelLg2
ORDER BY ?labelLg1