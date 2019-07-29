WITH <${uriGraph}>

DELETE {
	?uri <http://schema.org/url> ?url .
		}
WHERE 	{	
	 ?uri rdf:type foaf:Document .
	?uri <http://schema.org/url> ?url .
	FILTER(?url = <${oldUrl}>)
	FILTER(STRENDS(STR(?uri), '${id}'))	
}
;

WITH <${uriGraph}>

INSERT {
	?uri <http://schema.org/url> <${newUrl}>
	}
WHERE 	{	
	 ?uri rdf:type foaf:Document .
	 FILTER(STRENDS(STR(?uri), '${id}'))	
	 }
	 
	 