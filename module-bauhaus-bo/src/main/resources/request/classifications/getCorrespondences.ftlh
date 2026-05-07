SELECT DISTINCT ?id ?label
WHERE {
    ?correspondence rdf:type xkos:Correspondence .
    ?correspondence skos:prefLabel ?label .
    FILTER (lang(?label) = '${LG1}')
    BIND(STRAFTER(STR(?correspondence),'/codes/') AS ?id)
}
ORDER BY ?label
