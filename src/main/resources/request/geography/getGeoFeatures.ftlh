SELECT DISTINCT ?id ?uri ?labelLg1 ?labelLg2 ?code ?typeTerritory ?dateCreation ?dateSuppression ?hasComposition
FROM <${COG_GRAPH}>
FROM <${GEO_SIMS_GRAPH}>
 WHERE { 
		{	?uri igeo:nom ?labelLg1 .
			?uri igeo:nom ?labelLg2 .
			?uri igeo:codeINSEE ?code .

			OPTIONAL {
				?evntCreation igeo:creation ?uri .
				?evntCreation igeo:date ?dateCreation .
			}
			OPTIONAL {
				?evntSuppression igeo:suppression ?uri .
				?evntSuppression igeo:date ?dateSuppression.
			}

			OPTIONAL {
				?uri a ?typeTerritoryUri .
				BIND(STRAFTER(str(?typeTerritoryUri),"geo#") AS ?typeTerritory)
			}
			FILTER (!BOUND(?typeTerritory) || ?typeTerritory IN ("Region","Departement"))

	} 
	UNION
	
	{
			OPTIONAL {
				?uri skos:prefLabel ?labelLg1 .
				FILTER (lang(?labelLg1) = '${LG1}')
			}

			OPTIONAL {
				?uri skos:prefLabel ?labelLg2 .
				FILTER (lang(?labelLg2) = '${LG2}')
			}

			?uri a 	igeo:TerritoireStatistique .
			BIND("Territoire Statistique" AS ?typeTerritory)

	}	
		UNION
	
	{
			?uri rdfs:label ?labelLg1 .
			?uri rdfs:label ?labelLg2 .
			?uri a 	igeo:TerritoireFrancais .
			BIND("Territoire Français" AS ?typeTerritory)

	}
		UNION
	
	{
			?uri rdfs:label ?labelLg1 .
			?uri rdfs:label ?labelLg2 .
			?uri a 	igeo:Pays .
			BIND("pays" AS ?typeTerritory)

	}		
			
			
			BIND(REPLACE( STR(?uri) , '(.*/)(\\w.*$)', '$2' ) AS ?id)

			BIND(EXISTS{?uri geo:union ?unionFeat}||EXISTS{?uri geo:difference ?diffFeat} AS ?hasComposition)
			
<#if uriFeature != "">
			   FILTER (?uri = <${uriFeature}>)
</#if>
		
		}
ORDER BY ?code
		