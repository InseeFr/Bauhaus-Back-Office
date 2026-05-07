SELECT ?id ?sourceLabelLg1 ?sourceLabelLg2 ?sourceId
?targetLabelLg1 ?targetLabelLg2 ?targetId
WHERE {
    ?correspondence xkos:madeOf ?association .
    FILTER(REGEX(STR(?correspondence),'/codes/${CORRESPONDENCE_ID}')) .
    BIND(STRAFTER(STR(?association),'/association/') AS ?id) .
    ?association xkos:sourceConcept ?source .
    ?source skos:notation ?sourceId .
    ?source skos:prefLabel ?sourceLabelLg1 .
    FILTER (lang(?sourceLabelLg1) = '${LG1}')  .
    OPTIONAL {?source skos:prefLabel ?sourceLabelLg2 .
    FILTER (lang(?sourceLabelLg2) = '${LG2}') }  .
    ?association xkos:targetConcept ?target .
    ?target skos:notation ?targetId .
    ?target skos:prefLabel ?targetLabelLg1 .
    FILTER (lang(?targetLabelLg1) = '${LG1}')  .
    OPTIONAL {?target skos:prefLabel ?targetLabelLg2 .
    FILTER (lang(?targetLabelLg2) = '${LG2}') }  .
}
