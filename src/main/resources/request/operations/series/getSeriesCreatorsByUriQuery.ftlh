SELECT ?creators 
	WHERE	{ 
		?series dc:creator ?creators . 
		VALUES ?series { ${URI_SERIES}} 
	}