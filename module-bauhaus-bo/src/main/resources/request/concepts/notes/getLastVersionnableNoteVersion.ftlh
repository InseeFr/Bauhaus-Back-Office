select ?version where {
?concept ${PREDICAT} ?note .
FILTER(REGEX(STR(?concept),${CONCEPT_URI_PATTERN})) .
?note pav:version ?version }
ORDER BY DESC(?version)
LIMIT 1