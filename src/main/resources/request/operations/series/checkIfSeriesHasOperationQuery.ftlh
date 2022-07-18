ASK
 WHERE 
  { graph <${OPERATIONS_GRAPH}>   
  	{ ?op <http://purl.org/dc/terms/isPartOf> <${URI_SERIES}> .
  	  ?op a <http://rdf.insee.fr/def/base#StatisticalOperation> .	}
  }