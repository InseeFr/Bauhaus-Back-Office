SELECT ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational 
  FROM <http://rdf.insee.fr/graphes/def/simsv2fr> 
  FROM <http://rdf.insee.fr/graphes/codes> 
  WHERE { 
	  ?mas rdfs:label ?masLabelLg1 ; 
	  rdfs:label ?masLabelLg2 ; 
	  FILTER(lang(?masLabelLg1) = '${LG1}') 
	  FILTER(lang(?masLabelLg2) = '${LG2}') 
	  
	  OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational } 
									
	  ?mas sdmx-mm:metadataAttributeProperty ?map . 
	  OPTIONAL {?map rdfs:range ?range } 
									
	FILTER(STRENDS(STR(?mas), '${idMas}')) 

  } 
  ORDER BY ?mas