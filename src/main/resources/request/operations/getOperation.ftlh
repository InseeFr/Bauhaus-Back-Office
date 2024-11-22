SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2 ?idSims ?validationState ?created ?modified ?year
WHERE { GRAPH <${OPERATIONS_GRAPH}> {
    ?operation skos:prefLabel ?prefLabelLg1 .
    FILTER(STRENDS(STR(?operation),'/operations/operation/${ID}')) .
    BIND(STRAFTER(STR(?operation),'/operation/') AS ?id) .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    OPTIONAL {
        ?operation skos:prefLabel ?prefLabelLg2 .
        FILTER (lang(?prefLabelLg2) = '${LG2}')
    } .
    OPTIONAL {
        ?operation skos:altLabel ?altLabelLg1 .
        FILTER (lang(?altLabelLg1) = '${LG1}')
    } .
    OPTIONAL {
        ?operation skos:altLabel ?altLabelLg2 .
        FILTER (lang(?altLabelLg2) = '${LG2}') } .
    }
    OPTIONAL { ?operation dcterms:created ?created } .
    OPTIONAL { ?operation dcterms:modified ?modified } .
    OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport . ?report sdmx-mm:target ?operation  BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . }
    OPTIONAL {?operation insee:validationState ?validationState .}
    OPTIONAL {?operation insee:year ?year .}
}
LIMIT 1