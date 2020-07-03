SELECT DISTINCT ?uri ?labelLg1 ?labelLg2 ?code ?descriptionLg1 ?descriptionLg2 ?typeTerritory ?dateCreation ?dateSuppression ?hasComposition
FROM <${COG_GRAPH}>
FROM <${GEO_SIMS_GRAPH}>
 WHERE { 
			?uri a ?typeTerritory ;
				 igeo:nom ?labelLg1 ;
				 igeo:codeINSEE ?code .

			OPTIONAL {
				?evenementCreation igeo:creation ?uri ;
					               igeo:date ?dateCreation .
			}
			OPTIONAL {
				?evenementSuppression igeo:suppression ?uri ;
					                  igeo:date ?dateSuppression.
			}

			BIND(EXISTS{?uri geo:union ?unionFeat} AS ?hasComposition)
<#if uriFeature != "">
			   FILTER (?uri = ${uriFeature})
</#if>
		}
ORDER BY ?code
		