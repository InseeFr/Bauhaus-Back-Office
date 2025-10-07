SELECT  ?labelLg1 ?labelLg2
WHERE  {
    GRAPH <${CODES_LISTS_GRAPH}> {
		?codeList rdf:type skos:ConceptScheme .
		?codeList skos:notation '${NOTATION}' .
		?item skos:inScheme ?codeList .
		?item skos:notation '${CODE}' .
		?item skos:prefLabel ?labelLg1 .
	    FILTER (lang(?labelLg1) = '${LG1}') .
        ?iri skos:prefLabel ?labelLg2 .
        FILTER (lang(?labelLg2) = '${LG2}') .
		}
	}