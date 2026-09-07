SELECT ?idOperation ?idSeries ?idIndicator
FROM ${DOCUMENTATION_GRAPH_IRI}
WHERE { 
 ?report rdf:type sdmx-mm:MetadataReport .
 ?report sdmx-mm:target ?target . 
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/operation/') AS ?idOperation) . }
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/serie/') AS ?idSeries) . }
 OPTIONAL{ BIND(STRAFTER(STR(?target),'/indicateur/') AS ?idIndicator) . }

 }