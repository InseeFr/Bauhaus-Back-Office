SELECT ?idOperation ?idSeries ?idIndicator
FROM <http://rdf.insee.fr/graphes/qualite/rapport/${idSims}>
WHERE { 
 ?report rdf:type sdmx-mm:MetadataReport .
 ?report sdmx-mm:target ?target . 
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/operation/') AS ?idOperation) . }
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/serie/') AS ?idSeries) . }
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/indicateur/') AS ?idIndicator) . }

 }