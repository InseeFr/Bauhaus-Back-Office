SELECT ?state 
FROM ${DOCUMENTATION_GRAPH_IRI}  
  WHERE {  
	?doc insee:validationState ?state . 
	 FILTER(STRENDS(STR(?doc), ${idSims}))
  }  