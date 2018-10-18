SELECT ?idOperation 
FROM <http://rdf.insee.fr/graphes/qualite/rapport/${idSims}>
WHERE { 
 ?report rdf:type sdmx-mm:MetadataReport .
 ?report sdmx-mm:target ?operation . 
 BIND(STRAFTER(STR(?operation),'/operation/') AS ?idOperation) . 
 }