SELECT ('${ID}' AS ?id) ?prefLabelLg1 ?prefLabelLg2 ?abstractLg1 ?abstractLg2 ?validationState ?created ?modified
WHERE {
    GRAPH <${OPERATIONS_GRAPH}> {
        ?family skos:prefLabel ?prefLabelLg1 .
        FILTER(STRENDS(STR(?family),'/operations/famille/${ID}')) .

        FILTER (lang(?prefLabelLg1) = '${LG1}') .

        OPTIONAL {?family skos:prefLabel ?prefLabelLg2 .
        FILTER (lang(?prefLabelLg2) = '${LG2}') } .

        OPTIONAL {?family insee:validationState ?validationState} .
        OPTIONAL { ?family dcterms:created ?created } .
        OPTIONAL { ?family dcterms:modified ?modified } .

<#if !FAMILIES_RICH_TEXT_NEXT_STRUCTURE>
    OPTIONAL {
        ?family dcterms:abstract ?abstractLg1Uri .
        ?abstractLg1Uri evoc:noteLiteral ?abstractLg1 .
        ?abstractLg2Uri xsd:language '${LG1}'^^xsd:language
    }
    OPTIONAL {
        ?family dcterms:abstract ?abstractLg2Uri .
        ?abstractLg2Uri evoc:noteLiteral ?abstractLg2 .
        ?abstractLg2Uri xsd:language '${LG2}'^^xsd:language
    }
</#if>
        OPTIONAL {?family dcterms:abstract ?abstractLg1 .
        FILTER (lang(?abstractLg1) = '${LG1}') } .
        OPTIONAL {?family dcterms:abstract ?abstractLg2 .
        FILTER (lang(?abstractLg2) = '${LG2}') } .
    }
}
LIMIT 1