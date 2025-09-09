
SELECT ?id ?typeOfLink ?prefLabelLg1 ?prefLabelLg2 

		WHERE {  
				 		
				 {<${uriConcept}> skos:narrower ?ConceptLinked . 
				 BIND('broader' AS ?typeOfLink)} 
				
				 UNION
				 {<${uriConcept}> skos:broader ?ConceptLinked . 
				 BIND('narrower' AS ?typeOfLink)} 
				 
				 UNION
				 {<${uriConcept}> dcterms:references ?ConceptLinked . 
				 BIND('references' AS ?typeOfLink)} 
				 
				 UNION
				 {<${uriConcept}> dcterms:replaces ?ConceptLinked . 
				 BIND('succeed' AS ?typeOfLink)} 
				 
				 UNION
				 {<${uriConcept}> skos:related ?ConceptLinked . 
				 BIND('related' AS ?typeOfLink)} 
				 
				 UNION
				{?ConceptLinked skos:narrower <${uriConcept}> .
				 BIND('narrower' AS ?typeOfLink)} 
				
				 UNION
				 {?ConceptLinked skos:broader <${uriConcept}> .
				 BIND('broader' AS ?typeOfLink)} 
				 
				 UNION
				 {?ConceptLinked dcterms:references <${uriConcept}> .
				 BIND('referenced' AS ?typeOfLink)} 
				 
				 UNION
				 {?ConceptLinked dcterms:replaces <${uriConcept}> .
				 BIND('precedes' AS ?typeOfLink)} 
				 
				 UNION
				 {?ConceptLinked skos:related <${uriConcept}> .
				 BIND('related' AS ?typeOfLink)}
				
				
				 ?ConceptLinked skos:prefLabel ?prefLabelLg1 . 
				 FILTER (lang(?prefLabelLg1) = "${LG1}") . 
				 OPTIONAL {?ConceptLinked skos:prefLabel ?prefLabelLg2 . 
				 FILTER (lang(?prefLabelLg2) = "${LG2}")} . 
				 BIND(STRAFTER(STR(?ConceptLinked),'/definition/') AS ?id) . 				
		 } 
				 
		 ORDER BY ?typeOfLink