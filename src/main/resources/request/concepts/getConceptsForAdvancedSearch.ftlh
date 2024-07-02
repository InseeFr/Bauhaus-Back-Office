SELECT DISTINCT ?id ?label ?created ?modified ?disseminationStatus ?validationStatus ?definition ?creator ?isTopConceptOf ?valid (group_concat(?altLabelLg1;separator=' || ') as ?altLabel)
WHERE
{
    GRAPH <${CONCEPTS_GRAPH}> {
        ?concept skos:notation ?notation .
        BIND (STR(?notation) AS ?id)
        ?concept skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}') .

        OPTIONAL {
            ?concept skos:altLabel ?altLabelLg1 .
            FILTER (lang(?altLabelLg1) = '${LG1}')
        }
        ?concept dcterms:created ?created .
        BIND (exists{?concept skos:broader ?broa} AS ?broader) .
        BIND (IF(?broader, 'false', 'true') AS ?isTopConceptOf) .

        OPTIONAL{?concept dcterms:modified ?modified} .
        OPTIONAL {?concept dcterms:valid ?valid} .

        ?concept insee:disseminationStatus ?disseminationStatus .
        ?concept insee:isValidated ?validationStatus .

        ?concept dc:creator ?creator .
        OPTIONAL{
            ?concept skos:definition ?noteUri .
            ?noteUri pav:version ?version .
            ?noteUri evoc:noteLiteral ?definition .
            ?noteUri dcterms:language '${LG1}'^^xsd:language .
            OPTIONAL {
                ?concept skos:definition ?latest .
                ?latest pav:version ?latestVersion .
                ?latest dcterms:language '${LG1}'^^xsd:language .
                FILTER (?version < ?latestVersion)
            } .
            FILTER (!bound (?latest))
        }
    }
}
GROUP BY ?id ?label ?created ?modified ?disseminationStatus ?validationStatus ?definition ?creator ?isTopConceptOf ?valid
ORDER BY ?label