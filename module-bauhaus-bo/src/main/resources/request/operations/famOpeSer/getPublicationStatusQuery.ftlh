SELECT ?state 
  FROM <${OPERATIONS_GRAPH}>
  WHERE {  
	?famopeser insee:validationState ?state . 
	 FILTER(STRENDS(STR(?famopeser), '${id}'))
  }  