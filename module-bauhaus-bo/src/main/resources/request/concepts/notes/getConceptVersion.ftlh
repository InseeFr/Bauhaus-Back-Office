select ?conceptVersion where {
?concept ?predicat ?note .
FILTER(REGEX(STR(?concept),'/concepts/definition/${CONCEPT_ID}')) .
?note insee:conceptVersion ?conceptVersion }
ORDER BY DESC(?conceptVersion)
LIMIT 1