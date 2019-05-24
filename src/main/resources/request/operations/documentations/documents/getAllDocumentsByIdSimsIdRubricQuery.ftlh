SELECT ?url ?labelLg1 ?labelLg2 ?updatedDate ?lang ?descriptionLg1 ?descriptionLg2 (?document AS ?uri)
FROM <http://rdf.insee.fr/graphes/qualite/rapport/${idSims}>
FROM <http://rdf.insee.fr/graphes/qualite/documents>

WHERE { 
	 ?text rdf:type dcmitype:Text .
	 ?text insee:additionalMaterial ?document . 
	 ?document rdf:type foaf:Document .
	 
	 ?document <http://schema.org/url> ?url
 
 	 OPTIONAL{ ?document rdfs:label ?labelLg1 . 
		FILTER(lang(?labelLg1) = '${LG1}') 
	 }
	 OPTIONAL{ ?document rdfs:label ?labelLg2 . 
		FILTER(lang(?labelLg2) = '${LG2}')  
	 }
	 
	 OPTIONAL{ ?document rdfs:comment ?descriptionLg1 . 
		FILTER(lang(?descriptionLg1) = '${LG1}') 
	 }
	 OPTIONAL{ ?document rdfs:comment ?descriptionLg2 . 
		FILTER(lang(?descriptionLg2) = '${LG2}')  
	 }
	 
	 
	 OPTIONAL{ ?document pav:lastRefreshedOn ?updatedDate .  }
	 OPTIONAL{ ?document dc:language ?lang .  }
	 
	FILTER(REGEX(STR(?text), '${idRubric}')) 

 }