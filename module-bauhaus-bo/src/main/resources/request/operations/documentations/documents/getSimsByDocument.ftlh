select ?id ?labelLg1 ?labelLg2 ?simsRubricId
where {
    ?simsValue insee:additionalMaterial ?document .
    FILTER (regex(str(?document), '${ID}$','i'))
    ?simsRubric ?p ?simsValue .
    ?simsRubric sdmx-mm:metadataReport ?sims .
    ?sims rdfs:label ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}') .
    ?sims rdfs:label ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') .
    BIND(STRAFTER(STR(?sims),'/qualite/rapport/') AS ?id) .
    BIND(REPLACE( STR(?simsRubric) , '(.*/)(\\w.*$)', '$2' ) AS ?simsRubricId) .
}
