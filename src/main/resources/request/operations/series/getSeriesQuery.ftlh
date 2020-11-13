SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel) 
<#if withSims = "true"> ?idSims
</#if>	
	WHERE { 
		GRAPH <${OPERATIONS_GRAPH}> { 
			 ?series a insee:StatisticalOperationSeries .   
			 ?series skos:prefLabel ?label . 
			 FILTER (lang(?label) = '${LG1}') 
			 BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . 
			 OPTIONAL{
			 	?series skos:altLabel ?altLabelLg1 .   
			 	FILTER (lang(?altLabelLg1) = '  ${LG1}') 
			 }
<#if withSims = "true"> 
			 OPTIONAL{ 
			 	?report rdf:type sdmx-mm:MetadataReport .   
			 	?report sdmx-mm:target ?series 
			 	BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .   
			 } 
</#if>				 
			 
		}
	}   
	GROUP BY ?id ?label   <#if withSims = "true"> ?idSims</#if>	
	ORDER BY ?label