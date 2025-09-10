SELECT ?state 
FROM <${DOCUMENTATIONS_GRAPH}/${idSims}>  
  WHERE {  
	?doc insee:validationState ?state . 
	 FILTER(STRENDS(STR(?doc), '${idSims}'))
  }  