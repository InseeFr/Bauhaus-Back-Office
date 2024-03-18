SELECT ?contributors
FROM <${STRUCTURES_GRAPH}>
	WHERE	{
		{
			?structure dc:contributor ?contributors .
			VALUES ?structure { ${URI_STRUCTURE?no_esc}}
		}

	}