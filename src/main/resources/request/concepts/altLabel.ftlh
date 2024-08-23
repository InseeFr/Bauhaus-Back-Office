SELECT ?altLabel
	WHERE {
		?concept skos:altLabel ?altLabel
		FILTER (lang(?altLabel) = '${LG}') .
		FILTER(REGEX(STR(?concept),'/concepts/definition/${ID}')) .
		}