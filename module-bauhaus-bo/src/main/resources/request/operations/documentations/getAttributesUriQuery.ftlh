SELECT ?id ?uri 
  FROM <${MSD_GRAPH}> 
  WHERE { 
	  ?uri rdf:type sdmx-mm:MetadataAttributeProperty ;
	  	   dc:identifier ?id
   } 
   ORDER BY ?id