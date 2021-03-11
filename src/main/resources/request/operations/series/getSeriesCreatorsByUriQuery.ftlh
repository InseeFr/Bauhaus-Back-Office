SELECT ?creators 
FROM <${OPERATIONS_GRAPH}>
	WHERE	{ 
		?series dc:creator ?creators . 
		VALUES ?series { <${URI_SERIES}>} 
	}