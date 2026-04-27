select ?version where {
?concept <${PREDICAT}> ?note .
FILTER(REGEX(STR(?concept),'/concepts/definition/${CONCEPT_ID}')) .
?note pav:version ?version }
ORDER BY DESC(?version)
LIMIT 1