SELECT ?narrowerId {
			?concept skos:notation '${ID}' .
			?concept skos:narrower ?narrower .
			?narrower skos:notation ?narrowerIdStr
			BIND (STR(?narrowerIdStr) AS ?narrowerId)
			FILTER(REGEX(STR(?concept),'/concepts/definition/${ID}')) .
		}