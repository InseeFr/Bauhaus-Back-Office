<#-- URI_STRUCTURE est une IRI complète (<…>), échappée par SparqlLiterals. -->
SELECT ?contributors
FROM ${STRUCTURES_GRAPH}
	WHERE	{
		{
			?structure dc:contributor ?contributors .
			VALUES ?structure { ${URI_STRUCTURE} }
		}
	}
