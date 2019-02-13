
WITH <${uriGraph}>

DELETE { <${uriConcept}> ?b ?c }

WHERE
  { <${uriConcept}> ?b ?c }
   
  ;
  
DELETE { ?a ?b <${uriConcept}> }

WHERE
  {  ?a ?b <${uriConcept}> } 