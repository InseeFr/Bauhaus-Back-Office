
WITH <${uriGraph}>

DELETE  {
			<${uriConcept}> skos:definition ?def .
			<${uriConcept}> skos:scopeNote ?sn .
			<${uriConcept}> skos:editorialNote ?en .
			<${uriConcept}> skos:changeNote ?cn . 
			<${uriConcept}> ?p ?c . 
			?def ?q ?z .
			?sn ?q ?z .
			?en ?q ?z .
			?cn ?q ?z .
		}
 WHERE {
       { <${uriConcept}> skos:definition ?def . 
        OPTIONAL { ?def ?q ?z }
       } UNION
	    { <${uriConcept}> skos:scopeNote ?sn . 
        OPTIONAL { ?sn ?q ?z }
       } UNION
	    { <${uriConcept}> skos:editorialNote ?en . 
        OPTIONAL { ?en ?q ?z }
       } UNION
	    { <${uriConcept}> skos:changeNote ?cn . 
        OPTIONAL { ?cn ?q ?z }
       } UNION
	    { <${uriConcept}> ?p ?c  
	   } 
	  }