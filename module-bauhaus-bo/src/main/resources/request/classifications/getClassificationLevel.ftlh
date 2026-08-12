SELECT ?classificationId ?levelId ?prefLabelLg1 ?prefLabelLg2 ?depth ?notation
?notationPattern ?broaderLg1 ?broaderLg2 ?idBroader ?narrowerLg1 ?narrowerLg2 ?idNarrower
WHERE {
?level rdf:type xkos:ClassificationLevel .
FILTER(STRENDS(STR(?level),'/codes/${CLASSIFICATION_ID}/${LEVEL_ID}'))
BIND(STRAFTER(STRAFTER(STR(?level),'/codes/'), '/') AS ?levelId)
BIND(STRBEFORE(STRAFTER(STR(?level),'/codes/'), '/') AS ?classificationId)
?level skos:prefLabel ?prefLabelLg1 .
FILTER (lang(?prefLabelLg1) = '${LG1}')
OPTIONAL {?level skos:prefLabel ?prefLabelLg2 .
FILTER (lang(?prefLabelLg2) = '${LG2}') }
?level xkos:depth ?depth .
?level skos:notation ?notation .
?level xkos:notationPattern ?notationPattern .
OPTIONAL {?node rdf:first ?level .
    OPTIONAL {?node rdf:rest ?nextNode .
    ?nextNode rdf:first ?nextLevel .
    ?nextLevel skos:prefLabel ?narrowerLg1 .
    FILTER (lang(?narrowerLg1) = '${LG1}')
        OPTIONAL {?nextLevel skos:prefLabel ?narrowerLg2 .
        FILTER (lang(?narrowerLg2) = '${LG2}') }
    BIND(STRAFTER(STRAFTER(STR(?nextLevel),'/codes/'), '/') AS ?idNarrower) }
    OPTIONAL {?previousNode rdf:rest ?node .
    ?previousNode rdf:first ?previousLevel .
    ?previousLevel skos:prefLabel ?broaderLg1 .
    FILTER (lang(?broaderLg1) = '${LG1}')
        OPTIONAL {?previousLevel skos:prefLabel ?broaderLg2 .
        FILTER (lang(?broaderLg2) = '${LG2}') }
    BIND(STRAFTER(STRAFTER(STR(?previousLevel),'/codes/'), '/') AS ?idBroader) }
}
}
