SELECT ?value ?label
FROM <http://rdf.insee.fr/graphes/adms>
WHERE {
    ?archiveUnitIdentifierUri rdf:type adms:Identifier .
    ?archiveUnitIdentifierUri skos:inScheme<http://bauhaus/identifierSchemes/uniteArchivageNamingScheme> .
    ?archiveUnitIdentifierUri skos:notation ?value .
    ?archiveUnitIdentifierUri skos:prefLabel ?label .
    FILTER(lang(?label) = '${LG1}') .
}