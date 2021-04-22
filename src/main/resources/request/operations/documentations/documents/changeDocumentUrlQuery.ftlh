WITH <${DOCUMENTS_GRAPH}>

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

WITH <${DOCUMENTS_GRAPH}>

INSERT {
	?uri <http://schema.org/url> <${newUrl}>
	}
WHERE 	{	
	 ?uri rdf:type foaf:Document .
	 FILTER(STRENDS(STR(?uri), '${id}'))	
	 }
	 
	 