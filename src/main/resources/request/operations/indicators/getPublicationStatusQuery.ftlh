SELECT ?state 
  FROM <http://rdf.insee.fr/graphes/produits>
  WHERE {  
	?indic insee:validationState ?state . 
	 FILTER(STRENDS(STR(?indic), '${id}'))
  }  