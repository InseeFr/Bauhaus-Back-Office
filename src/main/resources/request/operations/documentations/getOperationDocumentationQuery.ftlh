SELECT ?idSims 
 WHERE { 
	 ?report rdf:type sdmx-mm:MetadataReport .
	 ?report sdmx-mm:target ?operation 
	 FILTER(STRENDS(STR(?operation), '${idOperation}'))
	 BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
 }