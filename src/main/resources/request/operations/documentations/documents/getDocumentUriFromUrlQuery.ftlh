SELECT ?document
FROM <${graph}>
WHERE { 
	 ?document rdf:type foaf:Document .
	 ?document <http://schema.org/url> <${url}>
 }