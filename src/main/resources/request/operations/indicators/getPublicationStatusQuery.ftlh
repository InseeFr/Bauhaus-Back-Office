SELECT ?state 
  FROM <${PRODUCTS_GRAPH}>
  WHERE {  
	?indic insee:validationState ?state . 
	 FILTER(STRENDS(STR(?indic), '${id}'))
  }  