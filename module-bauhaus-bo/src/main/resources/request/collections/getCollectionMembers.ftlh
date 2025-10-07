SELECT ?id ?prefLabelLg1 ?prefLabelLg2
WHERE {
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection),'/concepts/definitions/${ID}')) .
    ?collection skos:member ?member .
    ?member skos:notation ?id .
    ?member skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    OPTIONAL {?member skos:prefLabel ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}')}
}