SELECT DISTINCT ?iri ?id ?label ?altLabel
<#if withSims = "true"> ?idSims
</#if>	
	WHERE { 
		GRAPH <${OPERATIONS_GRAPH}> { 
			 ?iri a insee:StatisticalOperationSeries .
			 ?iri skos:prefLabel ?label .
			 FILTER (lang(?label) = '${LG1}') 
			 BIND(STRAFTER(STR(?iri),'/operations/serie/') AS ?id) .
			 OPTIONAL{
			 	?iri skos:altLabel ?altLabel .
			 	FILTER (lang(?altLabel) = '${LG1}')
			 }
<#if withSims = "true"> 
			 OPTIONAL{ 
			 	?report rdf:type sdmx-mm:MetadataReport .   
			 	?report sdmx-mm:target ?iri
			 	BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .   
			 } 
</#if>				 
			 
		}
	}   
	ORDER BY ?label