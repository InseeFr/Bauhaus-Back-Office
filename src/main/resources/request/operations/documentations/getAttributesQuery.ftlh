SELECT ?id ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational  
  FROM <http://rdf.insee.fr/graphes/def/simsv2fr>  
  FROM <http://rdf.insee.fr/graphes/concepts/qualite>
  WHERE {  
	?mas  dc:identifier ?id .
	OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational }  
				
	?mas sdmx-mm:metadataAttributeProperty ?map . 
	OPTIONAL {?map rdfs:range ?range } 
	
	  ?map sdmx-mm:concept ?concept .
	  ?concept skos:prefLabel ?masLabelLg1 ; 
	  		   skos:prefLabel ?masLabelLg2 ; 
	  FILTER(lang(?masLabelLg1) = '${LG1}') 
	  FILTER(lang(?masLabelLg2) = '${LG2}') 
  }  
  ORDER BY ?mas