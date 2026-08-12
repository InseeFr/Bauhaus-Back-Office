SELECT  ?uri
WHERE { GRAPH <${CODES_LISTS_GRAPH}> {
?codeList rdf:type skos:ConceptScheme .
?codeList skos:notation '${NOTATION}' .
?uri skos:inScheme ?codeList .
?uri skos:notation '${CODE}' .
 }}
