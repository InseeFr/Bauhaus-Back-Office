SELECT ?idMas ?masLabelLg1 ?masLabelLg2 ?idParent ?isPresentational 
 FROM <http://rdf.insee.fr/graphes/def/simsv2fr> 
 WHERE { 
	 	?reportStructure sdmx-mm:metadataAttributeSpecification ?mas . 
	    ?mas rdfs:label ?masLabelLg1 ; 
      	     rdfs:label ?masLabelLg2 ; 
    	FILTER(lang(?masLabelLg1) = '${LG1}') 
    	FILTER(lang(?masLabelLg2) = '${LG2}') 

   		OPTIONAL {?mas sdmx-mm:parent ?parent } 
		BIND(REPLACE( STR(?parent) , '(.*/)(\\w.+$)', '$2' ) AS ?idParent) . 
		
   		OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational } 
		
		FILTER(STRENDS(STR(?reportStructure),'/qualite/simsv2fr/reportStructure')) . 
		BIND(REPLACE( STR(?mas) , '(.*/)(\\w.+$)', '$2' ) AS ?idMas) . 
  } 
 ORDER BY ?mas