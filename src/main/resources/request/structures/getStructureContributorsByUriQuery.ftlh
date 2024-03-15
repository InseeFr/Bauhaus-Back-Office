SELECT ?creators
FROM <${STRUCTURES_GRAPH}>
	WHERE	{
		{
			?structure dc:contributor ?STRUCTURES_GRAPH .
			VALUES ?structure { ${URI_STRUCTURE?no_esc}}
		}

	}