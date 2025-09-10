
	
SELECT DISTINCT ?src  
  WHERE
   { GRAPH ?src
    	{  
    	{ <${uriConcept}> ?b ?c} UNION
    	{ ?a <${uriConcept}> ?c} UNION
    	{ ?a ?b <${uriConcept}>}
    	}
	}