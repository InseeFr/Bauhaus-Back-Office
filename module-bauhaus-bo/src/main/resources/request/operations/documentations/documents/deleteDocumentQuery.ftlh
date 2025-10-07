WITH <${DOCUMENTS_GRAPH}>

DELETE  {
	 
	 ?uri rdf:type foaf:Document .
	 ?uri <http://schema.org/url> ?url .
 	 ?uri rdfs:label ?label . 
	 ?uri rdfs:comment ?description . 
	 ?uri pav:lastRefreshedOn ?updatedDate . 
	 ?uri dc:language ?lang .
	}
 WHERE {
   
	 ?uri rdf:type foaf:Document .
	 ?uri <http://schema.org/url> ?url .
 	OPTIONAL { ?uri rdfs:label ?label }
	OPTIONAL {  ?uri rdfs:comment ?description }
	OPTIONAL {  ?uri pav:lastRefreshedOn ?updatedDate } 
	OPTIONAL {  ?uri dc:language ?lang }
	 FILTER(?uri = <${uri}>) 
	  }
	; 
	 
DELETE {
	?text insee:additionalMaterial  ?uri .
	}
 WHERE {	
	?text insee:additionalMaterial  ?uri .
	 FILTER(?uri = <${uri}>) 
	}