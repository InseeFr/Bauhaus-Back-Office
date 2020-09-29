SELECT distinct ?publishers  
				
				FROM <${OPERATIONS_GRAPH}> 
				FROM <${ORGANIZATIONS_GRAPH}> 
				FROM <${ORG_INSEE_GRAPH}> 
				WHERE { 
					?series a insee:StatisticalOperationSeries .  
					?series dcterms:publisher ?uri  .  
					?uri dcterms:identifier  ?publishers .  
					 FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES} ')) .  
				} 