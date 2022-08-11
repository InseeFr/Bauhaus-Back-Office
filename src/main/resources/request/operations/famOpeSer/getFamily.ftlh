SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?abstractLg1 ?abstractLg2 ?validationState ?created ?modified
WHERE {
    GRAPH <${OPERATIONS_GRAPH}> {
        ?family skos:prefLabel ?prefLabelLg1 .
        FILTER(STRENDS(STR(?family),'/operations/famille/${ID}')) .
        BIND(STRAFTER(STR(?family),'/famille/') AS ?id) .

        FILTER (lang(?prefLabelLg1) = '${LG1}') .

        OPTIONAL {?family skos:prefLabel ?prefLabelLg2 .
        FILTER (lang(?prefLabelLg2) = '${LG2}') } .

        OPTIONAL {?family insee:validationState ?validationState} .
        OPTIONAL { ?family dcterms:created ?created } .
        OPTIONAL { ?family dcterms:modified ?modified } .
        OPTIONAL {?family dcterms:abstract ?abstractLg1 .
        FILTER (lang(?abstractLg1) = '${LG1}') } .
        OPTIONAL {?family dcterms:abstract ?abstractLg2 .
        FILTER (lang(?abstractLg2) = '${LG2}') } .
    }
}
LIMIT 1