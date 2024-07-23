SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?creator ?contributor ?disseminationStatus
		?additionalMaterial ?created ?modified ?valid ?conceptVersion ?isValidated
			WHERE { GRAPH <${CONCEPTS_GRAPH}>
			    {
				?concept skos:prefLabel ?prefLabelLg1 .

				FILTER(REGEX(STR(?concept),'/concepts/definition/${ID}')) .

				BIND(STRAFTER(STR(?concept),'/definition/') AS ?id) .
				?concept ?versionnedNote ?versionnedNoteURI .
				?versionnedNoteURI insee:conceptVersion ?conceptVersion .
 				?concept insee:isValidated ?isValidated .
				FILTER (lang(?prefLabelLg1) = '${LG1}') .
				OPTIONAL {?concept skos:prefLabel ?prefLabelLg2 .
				FILTER (lang(?prefLabelLg2) = '${LG2}') } .
				OPTIONAL {?concept dc:creator ?creator} .
				?concept dc:contributor ?contributor .
				?concept insee:disseminationStatus ?disseminationStatus
				OPTIONAL {?concept insee:additionalMaterial ?additionalMaterial} .
				?concept dcterms:created ?created .
				OPTIONAL {?concept dcterms:modified ?modified} .
				OPTIONAL {?concept dcterms:valid ?valid} .
				}
			}
				ORDER BY DESC(xsd:integer(?conceptVersion))
				LIMIT 1