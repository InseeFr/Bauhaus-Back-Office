SELECT ?note ?predicat
WHERE { GRAPH <${CONCEPTS_GRAPH}> {
?concept ?predicat ?note .
?note insee:conceptVersion ?conceptVersion
FILTER(?conceptVersion < ${MAX_VERSION})
FILTER(REGEX(STR(?concept),'/concepts/definition/${CONCEPT_ID}')) .
}}