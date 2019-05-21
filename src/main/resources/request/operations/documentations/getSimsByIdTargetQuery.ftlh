SELECT ?idSims 
 WHERE { 
	 ?report rdf:type sdmx-mm:MetadataReport .
	 ?report sdmx-mm:target ?target 
	 FILTER(STRENDS(STR(?target), '${idTarget}'))
	 BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
 }