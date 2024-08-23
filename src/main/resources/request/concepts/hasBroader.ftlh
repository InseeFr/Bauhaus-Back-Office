ASK {
	?concept skos:broader ?broader
	FILTER(REGEX(STR(?concept),'/concepts/definition/${ID}')) .
	}