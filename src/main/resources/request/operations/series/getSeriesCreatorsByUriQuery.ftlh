SELECT ?creators 
FROM <${OPERATIONS_GRAPH}>
	WHERE	{
		{
			?series dc:creator ?creators .
			VALUES ?series { ${URI_SERIES?no_esc}}
		}
		UNION
		{
			?series dc:creator ?creators .
			?series dcterms:hasPart ${URI_SERIES?no_esc}
		}
	}