SELECT distinct  ?creators 
<#if !SERIES_URI_SUFFIX??> 
	?idSerie
</#if>
	WHERE { 
		GRAPH ${OPERATIONS_GRAPH} {
				?series a insee:StatisticalOperationSeries . 
				<#if SERIES_URI_SUFFIX??> 
					 	FILTER(STRENDS(STR(?series),${SERIES_URI_SUFFIX})) . 
				</#if>
				
				?series dc:creator ?creators . 
			<#if !SERIES_URI_SUFFIX??> 
				BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?idSerie) .
			</#if>
				} 
			}