SELECT DISTINCT ?id
WHERE {
 GRAPH ?g {?doc a foaf:Document }
 BIND(STRAFTER(STR(?doc),'/documents/document/') AS ?id) .
}   
 GROUP BY ?g ?id
ORDER BY DESC(?id)
 LIMIT 1