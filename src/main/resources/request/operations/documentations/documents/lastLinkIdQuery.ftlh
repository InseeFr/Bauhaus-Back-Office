SELECT DISTINCT ?id
WHERE {
 GRAPH ?g {?doc a foaf:Document }
 BIND(STRAFTER(STR(?doc),'/documents/page/') AS ?id) .
}   
 GROUP BY ?g ?id
ORDER BY DESC(xsd:integer(?id))
 LIMIT 1