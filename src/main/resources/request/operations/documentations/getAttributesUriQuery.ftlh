SELECT ?id ?uri 
  FROM <http://rdf.insee.fr/graphes/def/simsv2fr> 
  WHERE { 
	  ?uri rdf:type sdmx-mm:MetadataAttributeProperty 
	  BIND(REPLACE( STR(?uri) , '(.*/)(\\w.+$)', '$2' ) AS ?id) . 
   } 
   ORDER BY ?id