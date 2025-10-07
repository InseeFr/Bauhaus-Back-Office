
SELECT ?id 
  where 
  {graph <${OPERATIONS_GRAPH}>   {   
  ?s a ?o
      BIND (xsd:integer(REPLACE(str(?s), "^http://(.*)/operations/(.*)/(.)", "")) AS ?id)
 } 
}
	ORDER BY DESC(?id)
	LIMIT 1