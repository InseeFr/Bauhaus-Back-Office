SELECT ?idMas ?masLabelLg1 ?masLabelLg2 ?idParent ?isPresentational 
 FROM <http://rdf.insee.fr/graphes/def/simsv2fr> 
 FROM <http://rdf.insee.fr/graphes/concepts/qualite>
 WHERE { 
	 	?reportStructure sdmx-mm:metadataAttributeSpecification ?mas . 

   		OPTIONAL {?mas sdmx-mm:parent ?parent } 
		BIND(REPLACE( STR(?parent) , '(.*/)(\\w.+$)', '$2' ) AS ?idParent) . 
		
   		OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational } 
		
		FILTER(STRENDS(STR(?reportStructure),'/qualite/simsv2fr/reportStructure')) . 
		BIND(REPLACE( STR(?mas) , '(.*/)(\\w.+$)', '$2' ) AS ?idMas) . 
		
		?mas sdmx-mm:metadataAttributeProperty ?map . 
	  	?map sdmx-mm:concept ?concept .
	  	?concept skos:prefLabel ?masLabelLg1 ; 
	  		    skos:prefLabel ?masLabelLg2 ; 
	  	FILTER(lang(?masLabelLg1) = '${LG1}') 
	  	FILTER(lang(?masLabelLg2) = '${LG2}') 
  } 
 ORDER BY ?mas