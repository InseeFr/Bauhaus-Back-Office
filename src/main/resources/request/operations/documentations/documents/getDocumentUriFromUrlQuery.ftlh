SELECT ?document
FROM <${DOCUMENTS_GRAPH}>
WHERE { 
	 ?document rdf:type foaf:Document .
	 ?document <http://schema.org/url> <${url}>
 }