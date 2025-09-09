SELECT ?value ?label
FROM <http://rdf.insee.fr/graphes/adms>
WHERE {
    ?value rdf:type adms:Identifier .
    ?value skos:inScheme <http://bauhaus/identifierSchemes/uniteArchivageNamingScheme> .
    ?value skos:prefLabel ?label .
    FILTER(lang(?label) = '${LG1}') .
}