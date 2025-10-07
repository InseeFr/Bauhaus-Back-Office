SELECT ?id ?prefLabelLg1 ?altLabelLg1 ?creator ?contributor ?disseminationStatus
				?additionalMaterial ?created ?modified ?valid ?conceptVersion ?isValidated ?defcourteLg1
				FROM <${CONCEPT_GRAPH}>
				 WHERE {
				 {
				 ?collection rdf:type skos:Collection .
				 FILTER(STRENDS(STR(?collection),'${COLLECTION_ID}')) .
				 ?collection skos:member ?member .
				 ?member skos:notation ?id .
				 ?member skos:prefLabel ?prefLabelLg1 .
				 FILTER (lang(?prefLabelLg1) = 'fr') .
        OPTIONAL {
            ?member skos:altLabel ?altLabelLg1 .
				 FILTER (lang(?altLabelLg1) = 'fr') .
            } }
    			OPTIONAL {?member insee:isValidated ?isValidated . }
        	    OPTIONAL {?member dc:creator ?creator .}
        	    OPTIONAL {?member dc:contributor ?contributor .}
        		OPTIONAL {	?member insee:disseminationStatus ?disseminationStatus .}
        		OPTIONAL {	?member insee:additionalMaterial ?additionalMaterial .}
        		OPTIONAL {	?member dcterms:created ?created .}
        		OPTIONAL {	?member dcterms:modified ?modified .}
        		OPTIONAL {	?member dcterms:valid ?valid .}
        		OPTIONAL {
          					Bind (IRI(concat('http://bauhaus/concepts/definition/',?id,'/definitionCourte/v1/fr')) as ?testFR ) .
            				OPTIONAL {?testFR evoc:noteLiteral ?defcourteLg1 .}

        OPTIONAL {	?member skos:notation ?id ;
        			skos:scopeNote ?noteUri .
            		?noteUri pav:version ?conceptVersion .

        }
    	 }




				} group by ?id ?prefLabelLg1  ?altLabelLg1  ?creator ?contributor ?disseminationStatus
				?additionalMaterial ?created ?modified ?valid ?conceptVersion ?isValidated ?defcourteLg1
									  ORDER BY ?id