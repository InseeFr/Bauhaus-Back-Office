<#-- URI_COMPONENT est une IRI complète (<…>), échappée par SparqlLiterals. -->
SELECT ?contributors
FROM ${STRUCTURES_COMPONENTS_GRAPH}
	WHERE	{
		{
			?component dc:contributor ?contributors .
			VALUES ?component { ${URI_COMPONENT} }
		}
	}
