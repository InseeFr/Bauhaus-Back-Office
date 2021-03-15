SELECT ?id ?typeOfLink ?prefLabelLg1 ?prefLabelLg2 ?urn 
WHERE { 
	GRAPH <${CONCEPTS_GRAPH} > { 
				
		?concept rdf:type skos:Concept . 
		FILTER(REGEX(STR(?concept),'/concepts/definition/${ID_CONCEPT}')) . 
					
		{
			?concept skos:narrower ?conceptlinked . 
			BIND('narrower' AS ?typeOfLink)
		} 
		UNION
		{
			?concept skos:broader ?conceptlinked . 
			BIND('broader' AS ?typeOfLink)
		} 
		UNION
		{
			?concept dcterms:references ?conceptlinked . 
			BIND('references' AS ?typeOfLink)
		} 
		UNION
		{
			?concept dcterms:replaces ?conceptlinked . 
			BIND('succeed' AS ?typeOfLink)
		} 
		UNION
		{
			?concept skos:related ?conceptlinked . 
			BIND('related' AS ?typeOfLink)
		} 
		UNION
		{
			?concept skos:closeMatch ?urn . 
			BIND('closeMatch' AS ?typeOfLink)
		} 
		
		OPTIONAL{
			?conceptlinked skos:prefLabel ?prefLabelLg1 . 
			FILTER (lang(?prefLabelLg1) = '${LG1}')
		} .
		OPTIONAL {
			?conceptlinked skos:prefLabel ?prefLabelLg2 . 
			FILTER (lang(?prefLabelLg2) = '${LG2}')
		} . 
		BIND(STRAFTER(STR(?conceptlinked),'/definition/') AS ?id) . 				
	}
} 
ORDER BY ?typeOfLink