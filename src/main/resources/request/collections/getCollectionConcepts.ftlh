select distinct ?id ?prefLabelLg1 ?prefLabelLg2
(group_concat(?altLabelLg1V;separator=' || ') as ?altLabelLg1) 
(group_concat(?altLabelLg2V;separator=' || ') as ?altLabelLg2) 
?isValidated ?creator ?contributor ?disseminationStatus ?additionalMaterial ?created ?modified ?valid ?conceptVersion ?defLongueLg1 ?defLongueLg2 ?defCourteLg1 ?defCourteLg2 ?editorialNoteLg1 ?editorialNoteLg2
from <${CONCEPT_GRAPH}>
			where { 
    {
				?collection rdf:type skos:Collection .
				FILTER(STRENDS(STR(?collection),'${COLLECTION_ID}')) .
				?collection skos:member ?member .
         	    ?member skos:notation ?id .
    			?member skos:prefLabel ?prefLabelLg1 . 
				 		FILTER (lang(?prefLabelLg1) = 'fr') . 
				 OPTIONAL {
        				?member skos:prefLabel ?prefLabelLg2 . 
				 		FILTER (lang(?prefLabelLg2) = 'en')
    					  }     		   
        	OPTIONAL {
            	 ?member skos:altLabel ?altLabelLg1V  
				 FILTER (lang(?altLabelLg1V) = 'fr')  
    				}
                OPTIONAL { 
        		?member skos:altLabel ?altLabelLg2V  
				 FILTER (lang(?altLabelLg2V) = 'en')
    				}
    			OPTIONAL {?member insee:isValidated ?isValidated . }
        	    OPTIONAL {?member dc:creator ?creator .}
        	    OPTIONAL {?member dc:contributor ?contributor .}
        		OPTIONAL {	?member insee:disseminationStatus ?disseminationStatus .}
        		OPTIONAL {	?member insee:additionalMaterial ?additionalMaterial .}
        		OPTIONAL {	?member dcterms:created ?created .}
        		OPTIONAL {	?member dcterms:modified ?modified .}
        		OPTIONAL {	?member dcterms:valid ?valid .}
    
   	    Optional { ?member skos:definition ?versionnedNoteURI .
        		?versionnedNoteURI dcterms:language 'fr'^^xsd:language .
				?versionnedNoteURI insee:conceptVersion ?conceptVersion.
        		
        filter not exists {
					?member skos:definition ?versionnedNoteURI2 . 
            		?versionnedNoteURI2 dcterms:language 'fr'^^xsd:language .
					?versionnedNoteURI2 insee:conceptVersion ?v2conceptVersion .
					filter (?v2conceptVersion > ?conceptVersion)
				}
    }
        OPTIONAL {
				?member skos:definition ?defFr . 
				?defFr dcterms:language 'fr'^^xsd:language . 
				?defFr evoc:noteLiteral ?defLongueLg1 .
				?defFr pav:version ?vdfr . 
				filter not exists {
					?member skos:definition ?def2Fr . 
					?def2Fr dcterms:language 'fr'^^xsd:language . 
					?def2Fr pav:version ?v2dfr .
					filter (?v2dfr > ?vdfr)
				}
			}  
			
			OPTIONAL {
				?member skos:definition ?defEn . 
				?defEn dcterms:language 'en'^^xsd:language . 
				?defEn evoc:noteLiteral ?defLongueLg2 .
				?defEn pav:version ?vden . 
				filter not exists {
					?member skos:definition ?def2En . 
					?def2En dcterms:language 'en'^^xsd:language . 
					?def2En pav:version ?v2den .
					filter (?v2den > ?vden)
				}
			}   
    OPTIONAL {
				?member skos:scopeNote ?scopeFr . 
				?scopeFr dcterms:language 'fr'^^xsd:language . 
				?scopeFr evoc:noteLiteral ?defCourteLg1 .
				?scopeFr pav:version ?vsfr .
				filter not exists {
					?member skos:scopeNote ?scope2Fr .
					?scope2Fr dcterms:language 'fr'^^xsd:language . 
					?scope2Fr pav:version ?v2sfr .
					filter (?v2sfr > ?vsfr)
				}				
			}   

			OPTIONAL {
				?member skos:scopeNote ?scopeEn . 
				?scopeEn dcterms:language 'en'^^xsd:language . 
				?scopeEn evoc:noteLiteral ?defCourteLg2 .
				?scopeEn pav:version ?vsen . 
				filter not exists {
					?member skos:scopeNote ?scope2En .
					?scope2En dcterms:language 'en'^^xsd:language . 
					?scope2En pav:version ?v2sen .
					filter (?v2sen > ?vsen)
				}
			} 

			OPTIONAL {
				?member skos:editorialNote ?editorialFr .
				?editorialFr dcterms:language 'fr'^^xsd:language .
				?editorialFr evoc:noteLiteral ?editorialNoteLg1 .
				?editorialFr pav:version ?vefr .
				filter not exists {
					?member skos:editorialNote ?editorial2Fr . 
					?editorial2Fr dcterms:language 'fr'^^xsd:language . 
					?editorial2Fr pav:version ?v2efr .
					filter (?v2efr > ?vefr)
				}				
			} 

			OPTIONAL {
				?member skos:editorialNote ?editorialEn .
				?editorialEn dcterms:language 'en'^^xsd:language .
				?editorialEn evoc:noteLiteral ?editorialNoteLg2 .
				?editorialEn pav:version ?veen .
				filter not exists {
					?member skos:editorialNote ?editorial2En . 
					?editorial2En dcterms:language 'en'^^xsd:language . 
					?editorial2En pav:version ?v2een .
					filter (?v2een > ?veen)
				}
	 		}
}
    
    
}   
group by  ?id ?prefLabelLg1 ?prefLabelLg2 ?isValidated ?creator ?contributor ?disseminationStatus ?additionalMaterial ?created ?modified ?valid ?conceptVersion ?defLongueLg1 ?defLongueLg2 ?defCourteLg1 ?defCourteLg2 ?editorialNoteLg1 ?editorialNoteLg2
 