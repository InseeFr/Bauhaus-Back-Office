select ?conceptVersion where {
?concept ?predicat ?note .
FILTER(REGEX(STR(?concept),${CONCEPT_URI_PATTERN})) .
?note insee:conceptVersion ?conceptVersion }
ORDER BY DESC(?conceptVersion)
LIMIT 1