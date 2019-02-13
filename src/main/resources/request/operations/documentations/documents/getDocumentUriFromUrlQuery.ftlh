SELECT ?document
FROM <${graph}>
WHERE { 
	 ?text rdf:type dcmitype:Text .
	 ?text insee:additionalMaterial ?document . 
	 ?document rdf:type foaf:Document .
	 
	 ?document <http://schema.org/url> <${url}>
 }