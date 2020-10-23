SELECT ?creators
	WHERE { 
		GRAPH <${OPERATIONS_GRAPH}> {
				?series a insee:StatisticalOperationSeries . 
			 	FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . 
				?series dc:creator ?creators . 
				} 
			}