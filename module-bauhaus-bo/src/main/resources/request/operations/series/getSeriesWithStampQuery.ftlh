SELECT DISTINCT ?id ?label (group_concat(?altLabelLg1;separator=' || ') as ?altLabel)

	WHERE {
		GRAPH <${OPERATIONS_GRAPH}> {
			 ?series a insee:StatisticalOperationSeries .
			 ?series skos:prefLabel ?label .

			 FILTER (lang(?label) = '${LG1}')
			 OPTIONAL {
				?series dc:creator ?creators
			 }

  			 BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) .
			 OPTIONAL{
			 	?series skos:altLabel ?altLabelLg1 .
			 	FILTER (lang(?altLabelLg1) = '${LG1}')
			 }

		}
		OPTIONAL {
			GRAPH <${ORG_INSEE_GRAPH}> {
				?creators adms:identifier ?creatorStamp .
			}
		}
		BIND(COALESCE(?creatorStamp, STR(?creators)) AS ?effectiveStamp)
<#if !ADMIN>
		FILTER (?effectiveStamp IN (<#list STAMP as s>"${s}"<#sep>, </#list>))
</#if>
	}
	GROUP BY ?id ?label
	ORDER BY ?label