SELECT ?state 
  FROM <http://rdf.insee.fr/graphes/operations>
  WHERE {  
	?family  rdf:type insee:StatisticalOperationFamily .
	?family insee:validationState ?state . 
	 FILTER(STRENDS(STR(?family), '${id}'))
  }  