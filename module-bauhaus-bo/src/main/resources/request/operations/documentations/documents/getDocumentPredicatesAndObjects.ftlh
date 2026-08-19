select ?predicat ?obj FROM ${DOCUMENTS_GRAPH}
WHERE {?document ?predicat ?obj .
FILTER (?document = ${uri})
}
