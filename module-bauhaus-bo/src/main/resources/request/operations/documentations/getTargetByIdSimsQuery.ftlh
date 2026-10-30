SELECT ?idOperation ?idSeries ?idIndicator
FROM ${DOCUMENTATION_GRAPH_IRI}
WHERE { 
 ?report rdf:type sdmx-mm:MetadataReport .
 ?report sdmx-mm:target ?target . 
 <#-- BIND hors OPTIONAL : le groupe d'un OPTIONAL ne voit pas ?target sous un dialecte SPARQL
      strict (Fuseki). STRAFTER rend "" quand le motif est absent, ce que l'appelant attend. -->
 BIND(STRAFTER(STR(?target),'/operation/') AS ?idOperation) .
 BIND(STRAFTER(STR(?target),'/serie/') AS ?idSeries) .
 BIND(STRAFTER(STR(?target),'/indicateur/') AS ?idIndicator) .

 }