SELECT ?labelLg1 ?labelLg2 ?idOperation ?idSeries ?idIndicator ?validationState
FROM <${DOCUMENTATIONS_GRAPH}/${idSims}>  
WHERE {  
	?report rdf:type sdmx-mm:MetadataReport . 				
		
	OPTIONAL{ ?report sdmx-mm:target ?operation .  BIND(STRAFTER(STR(?operation),'/operation/') AS ?idOperation) . } 
	OPTIONAL{ ?report sdmx-mm:target ?series .  BIND(STRAFTER(STR(?series),'/serie/') AS ?idSeries) . } 
	OPTIONAL{ ?report sdmx-mm:target ?indicator .  BIND(STRAFTER(STR(?indicator),'/indicateur/') AS ?idIndicator) . } 
			
	
	
	 OPTIONAL{ ?report rdfs:label ?labelLg1 . 
		FILTER(lang(?labelLg1) = '${LG1}') 
	 }
	 OPTIONAL{ ?report rdfs:label ?labelLg2 . 
		FILTER(lang(?labelLg2) = '${LG2}')  
	 }
	OPTIONAL{ ?report insee:validationState ?validationState .
	 }
	  						
	FILTER(STRENDS(STR(?report), '${idSims}')) 
}