SELECT ?definitionLg1 ?definitionLg2 ?scopeNoteLg1 ?scopeNoteLg2
		?editorialNoteLg1 ?editorialNoteLg2 ?changeNoteLg1 ?changeNoteLg2
		WHERE { GRAPH <${CONCEPTS_GRAPH}>
				{
				?concept skos:prefLabel ?prefLabelLg1 .
				FILTER(REGEX(STR(?concept),'/concepts/definition/${ID}')) .
				BIND(STRAFTER(STR(?concept),'/definition/') AS ?id) .
				# Def Lg1
				OPTIONAL {?concept skos:definition ?defLg1 .
				?defLg1 dcterms:language '${LG1}'^^xsd:language .
				?defLg1 evoc:noteLiteral ?definitionLg1 .
				?defLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
				} .
				# Def Lg2
				OPTIONAL {?concept skos:definition ?defLg2 .
				?defLg2 dcterms:language '${LG2}'^^xsd:language .
				?defLg2 evoc:noteLiteral ?definitionLg2 .
				?defLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
				} .
				# Def courte Lg1
				OPTIONAL {?concept skos:scopeNote ?scopeLg1 .
				?scopeLg1 dcterms:language '${LG1}'^^xsd:language .
				?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 .
				?scopeLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
				} .
				# Def courte Lg2
				OPTIONAL {?concept skos:scopeNote ?scopeLg2 .
				?scopeLg2 dcterms:language '${LG2}'^^xsd:language .
				?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 .
				?scopeLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
				} .
				# Note edit Lg1
				OPTIONAL {?concept skos:editorialNote ?editorialLg1 .
				?editorialLg1 dcterms:language '${LG1}'^^xsd:language .
				?editorialLg1 evoc:noteLiteral ?editorialNoteLg1 .
				?editorialLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
				} .
				# Note edit Lg2
				OPTIONAL {?concept skos:editorialNote ?editorialLg2 .
				?editorialLg2 dcterms:language '${LG2}'^^xsd:language .
				?editorialLg2 evoc:noteLiteral ?editorialNoteLg2 .
				?editorialLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
				} .
				# Note changement Lg1
				OPTIONAL {?concept skos:changeNote ?noteChangeLg1 .
				?noteChangeLg1 dcterms:language '${LG1}'^^xsd:language .
				?noteChangeLg1 evoc:noteLiteral ?changeNoteLg1 .
				?noteChangeLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int} .
				# Note changement Lg2
				OPTIONAL {?concept skos:changeNote ?noteChangeLg2 .
				?noteChangeLg2 dcterms:language '${LG2}'^^xsd:language .
				?noteChangeLg2 evoc:noteLiteral ?changeNoteLg2 .
				?noteChangeLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int} .
				}
		}