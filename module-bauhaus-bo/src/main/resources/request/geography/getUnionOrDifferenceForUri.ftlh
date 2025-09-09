SELECT DISTINCT ?uri ?labelLg1 ?code
FROM <${COG_GRAPH}>
FROM <${GEO_SIMS_GRAPH}>
 WHERE { 
		
<#if union>
		    <${uri}>  geo:union ?uri .
			
<#else>
 			<${uri}>  geo:difference ?uri .
</#if>

			
    ?uri (igeo:nom|rdfs:label|skos:prefLabel) ?labelLg1 .
    OPTIONAL{
			?uri igeo:codeINSEE ?code .
    }

}
ORDER BY ?code
		