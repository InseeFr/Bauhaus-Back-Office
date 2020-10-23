SELECT ?owner 
	WHERE	{  
				?series dcterms:publisher ?owner .  
				VALUES ?series { ${URI_SERIES} } 
			}