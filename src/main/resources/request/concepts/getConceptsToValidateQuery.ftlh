ELECT DISTINCT ?id ?label ?creator ?valid
	WHERE {
	    GRAPH <${CONCEPTS_GRAPH> {
		    ?concept rdf:type skos:Concept .
		    BIND(STRAFTER(STR(?concept),'/concepts/definition/') AS ?id) .
		    ?concept skos:prefLabel ?label .
		    ?concept dc:creator ?creator .
		    ?concept insee:isValidated 'false'^^xsd:boolean .
		    OPTIONAL {?concept dcterms:valid ?valid .}
		FILTER (lang(?label) = '" + config.getLg1() + "')
		}
	}
ORDER BY ?label