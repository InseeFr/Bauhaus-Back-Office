SELECT ?url ?labelLg1 ?labelLg2 ?lastRefresh ?lang
FROM <http://rdf.insee.fr/graphes/qualite/rapport/${idSims}>
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
	 
	 OPTIONAL{ ?document pav:lastRefreshedOn ?lastRefresh .  }
	 OPTIONAL{ ?document dc:language ?lang .  }
	 
	FILTER(REGEX(STR(?text), '${idRubric}')) 

 }