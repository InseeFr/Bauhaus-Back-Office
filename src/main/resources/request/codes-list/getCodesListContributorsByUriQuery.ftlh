SELECT ?creators
FROM <${CODES_LISTS_GRAPH}>
	WHERE	{
		{
			?codesList dc:contributor ?CODES_LISTS_GRAPH .
			VALUES ?codesList { ${URI_CODESLIST?no_esc}}
		}
		UNION
		{
			?series dc:creator ?creators .
			?series dcterms:hasPart ${URI_SERIES?no_esc}
		}
	}