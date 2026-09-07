SELECT ?altLabel
	WHERE {
		?concept skos:altLabel ?altLabel
		FILTER (lang(?altLabel) = ${LG}) .
		FILTER(REGEX(STR(?concept),${CONCEPT_URI_PATTERN})) .
		}