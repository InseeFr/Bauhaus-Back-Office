SELECT ?idMas ?masLabelLg1 ?masLabelLg2 ?idParent ?isPresentational 
 FROM <http://rdf.insee.fr/graphes/def/simsv2fr> 
 WHERE { 
	 	?reportStructure sdmx-mm:metadataAttributeSpecification ?mas . 
	    ?mas rdfs:label ?masLabelLg1 ; 
      	     rdfs:label ?masLabelLg2 ; 
    	FILTER(lang(?masLabelLg1) = '${LG1}') 
    	FILTER(lang(?masLabelLg2) = '${LG2}') 

   		OPTIONAL {?mas sdmx-mm:parent ?parent } 
		?parent dc:identifier ?idParent .
		
   		OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational } 
		
		FILTER(STRENDS(STR(?reportStructure),'/qualite/simsv2fr/reportStructure')) . 
		?mas dc:identifier ?idMas . 
  } 
 ORDER BY ?mas