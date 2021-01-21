SELECT ?seriesId 
	WHERE	{ 
		?series dc:creator ?creators  
		FILTER ( STR(?creators) = "${STAMP}")
		
		BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?seriesId)
	}