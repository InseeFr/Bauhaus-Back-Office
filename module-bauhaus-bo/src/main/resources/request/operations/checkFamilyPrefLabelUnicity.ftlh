<#-- Les valeurs venant de l'utilisateur sont des jetons SPARQL complets, échappés par SparqlLiterals :
     ne pas les entourer de quotes ici. -->
ASK
FROM ${OPERATIONS_GRAPH}
WHERE {
    ?uri a ${TYPE} .
    {?uri skos:prefLabel ${LABEL} .} UNION {?uri rdfs:label ${LABEL} .}
    FILTER(!STRENDS(STR(?uri), ${URI_SUFFIX})) .
}
