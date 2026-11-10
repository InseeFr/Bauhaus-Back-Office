SELECT ?url ?labelLg1 ?labelLg2 ?commentLg1 ?commentLg2 ?updatedDate ?lang ?size
FROM ${DOCUMENTS_GRAPH}
WHERE {
	${URI} rdf:type foaf:Document ;
		<http://schema.org/url> ?url .

	OPTIONAL { ${URI} rdfs:label ?labelLg1 . FILTER(lang(?labelLg1) = ${LG1}) }
	OPTIONAL { ${URI} rdfs:label ?labelLg2 . FILTER(lang(?labelLg2) = ${LG2}) }
	OPTIONAL { ${URI} rdfs:comment ?commentLg1 . FILTER(lang(?commentLg1) = ${LG1}) }
	OPTIONAL { ${URI} rdfs:comment ?commentLg2 . FILTER(lang(?commentLg2) = ${LG2}) }
	OPTIONAL { ${URI} pav:lastRefreshedOn ?updatedDate . }
	OPTIONAL { ${URI} dc:language ?lang . }
	OPTIONAL { ${URI} dcterms:extent ?size . }
}
LIMIT 1
