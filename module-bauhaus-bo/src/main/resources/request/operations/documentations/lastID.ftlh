SELECT DISTINCT ?idSims 
WHERE {
GRAPH ?g {?report a sdmx-mm:MetadataReport }
 BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
}   
 GROUP BY ?g ?idSims 
ORDER BY DESC(xsd:integer(?idSims))
 LIMIT 1