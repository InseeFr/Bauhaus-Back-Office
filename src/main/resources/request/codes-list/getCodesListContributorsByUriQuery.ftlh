SELECT ?contributors
FROM <${CODES_LISTS_GRAPH}>
	WHERE	{
		{
			?codesList dc:contributor ?contributors .
			VALUES ?codesList { ${URI_CODESLIST?no_esc}}
		}

	}
