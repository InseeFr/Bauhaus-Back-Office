SELECT ?state 
  FROM <http://rdf.insee.fr/graphes/operations>
  WHERE {  
	?famopeser insee:validationState ?state . 
	 FILTER(STRENDS(STR(?famopeser), '${id}'))
  }  