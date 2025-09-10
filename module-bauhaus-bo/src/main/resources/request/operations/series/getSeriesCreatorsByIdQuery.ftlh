SELECT distinct  ?creators 
<#if ID_SERIES = ""> 
	?idSerie
</#if>
	WHERE { 
		GRAPH <${OPERATIONS_GRAPH}> {
				?series a insee:StatisticalOperationSeries . 
				<#if ID_SERIES != ""> 
					 	FILTER(STRENDS(STR(?series),'/operations/serie/${ID_SERIES}')) . 
				</#if>
				
				?series dc:creator ?creators . 
			<#if ID_SERIES = ""> 
				BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?idSerie) .
			</#if>
				} 
			}