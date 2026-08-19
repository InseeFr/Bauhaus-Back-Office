ASK {
	?concept skos:broader ?broader
	FILTER(REGEX(STR(?concept),${CONCEPT_URI_PATTERN})) .
	}