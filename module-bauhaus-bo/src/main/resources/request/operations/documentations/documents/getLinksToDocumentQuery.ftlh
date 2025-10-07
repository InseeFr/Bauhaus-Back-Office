SELECT 
	?text 
 WHERE {	
	?text insee:additionalMaterial  ?uri .
	 FILTER(STRENDS(STR(?uri), '${id}'))
	}