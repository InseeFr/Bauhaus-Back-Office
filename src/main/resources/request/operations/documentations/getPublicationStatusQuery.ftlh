SELECT ?state 
  FROM <http://rdf.insee.fr/graphes/produits>
  WHERE {  
	?doc insee:validationState ?state . 
	 FILTER(STRENDS(STR(?doc), '${id}'))
  }  