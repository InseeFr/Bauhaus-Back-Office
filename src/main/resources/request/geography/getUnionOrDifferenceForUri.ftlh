SELECT DISTINCT ?uri ?labelLg1 ?labelLg2 ?code ?descriptionLg1 ?descriptionLg2 ?typeTerritory ?dateCreation ?dateSuppression ?hasComposition
FROM <${COG_GRAPH}>
FROM <${GEO_SIMS_GRAPH}>
 WHERE { 
		
<#if union>
		    ${uri}  geo:union ?uri .
			
<#else>
 			${uri}  geo:difference ?uri .
</#if>

			?uri igeo:nom ?labelLg1 ;
				 igeo:codeINSEE ?code .

		}
ORDER BY ?code
		