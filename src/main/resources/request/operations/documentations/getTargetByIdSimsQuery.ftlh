SELECT ?idOperation ?idSeries ?idIndicator
FROM <${DOCUMENTATIONS_GRAPH}/${idSims}>
WHERE { 
 ?report rdf:type sdmx-mm:MetadataReport .
 ?report sdmx-mm:target ?target . 
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/operation/') AS ?idOperation) . }
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/serie/') AS ?idSeries) . }
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/indicateur/') AS ?idIndicator) . }

 }