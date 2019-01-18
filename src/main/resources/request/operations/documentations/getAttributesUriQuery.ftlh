SELECT ?id ?uri 
  FROM <http://rdf.insee.fr/graphes/def/simsv2fr> 
  WHERE { 
	  ?uri rdf:type sdmx-mm:MetadataAttributeProperty ;
	  	   dc:identifier ?id
   } 
   ORDER BY ?id