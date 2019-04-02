
SELECT ?id 
  where 
  {graph <http://rdf.insee.fr/graphes/operations>   {   
  ?s a ?o
      BIND (xsd:integer(REPLACE(str(?s), "^http://bauhaus/operations/(.*)/(.)", "")) AS ?id)
 } 
}
	ORDER BY DESC(?id) 
	LIMIT 1