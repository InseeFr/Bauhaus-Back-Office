SELECT ?state 
  WHERE {  
	?doc insee:validationState ?state . 
	 FILTER(STRENDS(STR(?doc), '${id}'))
  }  