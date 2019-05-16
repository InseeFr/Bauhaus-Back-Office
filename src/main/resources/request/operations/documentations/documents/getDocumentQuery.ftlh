SELECT ?uri ?url ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?updatedDate ?lang

WHERE { 
	 ?text rdf:type dcmitype:Text .
	 ?text insee:additionalMaterial ?uri . 
	 ?uri rdf:type foaf:Document .
	 
	 ?uri <http://schema.org/url> ?url
 
 	 OPTIONAL{ ?uri rdfs:label ?labelLg1 . 
		FILTER(lang(?labelLg1) = '${LG1}') 
	 }
	 OPTIONAL{ ?uri rdfs:label ?labelLg2 . 
		FILTER(lang(?labelLg2) = '${LG2}')  
	 }
	 
	 OPTIONAL{ ?uri pav:lastRefreshedOn ?updatedDate .  }
	 OPTIONAL{ ?uri dc:language ?lang .  }
	 
	BIND(REPLACE( STR(?uri) , '(.*/)(\\w.+$)', '$2' ) AS ?id) .  
	FILTER(?id = '${id}') 

 }