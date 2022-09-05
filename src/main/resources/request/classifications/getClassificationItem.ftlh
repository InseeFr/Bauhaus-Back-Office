SELECT ?classificationId ?itemId ?prefLabelLg1 ?prefLabelLg2 ?isValidated
?broaderLg1 ?broaderLg2 ?idBroader ?conceptVersion ?altLabelLg1 ?altLabelLg2
WHERE {
    ?item skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}')

    ?item skos:inScheme ?classification .
    FILTER(REGEX(STR(?classification),'/codes/${CLASSIFICATION_ID}/'))
    FILTER(STRENDS(STR(?item),'/${ITEM_ID}'))
    BIND('${CLASSIFICATION_ID}' as ?classificationId)

    OPTIONAL {
        ?item skos:altLabel ?altLabelLg1 .
        FILTER (lang(?altLabelLg1) = '${LG1}')
    } .

    OPTIONAL {
        ?item skos:altLabel ?altLabelLg2 .
        FILTER (lang(?altLabelLg2) = '${LG2}')
    } .

    OPTIONAL {
        ?item skos:prefLabel ?prefLabelLg2 .
        FILTER (lang(?prefLabelLg2) = '${LG2}')
    } .

    ?item skos:notation ?itemId .
    ?item insee:isValidated ?isValidated .

    OPTIONAL {
        ?item ?versionnedNote ?versionnedNoteURI .
        ?versionnedNoteURI insee:conceptVersion ?conceptVersion .
    }


    OPTIONAL {
        ?item skos:broader ?broaderURI .
        ?broaderURI skos:prefLabel ?broaderLg1 .
        FILTER (lang(?broaderLg1) = '${LG1}')
        ?broaderURI skos:notation ?idBroader .
    }

    OPTIONAL {
        ?item skos:broader ?broaderURI .
        ?broaderURI skos:prefLabel ?broaderLg2 .
        FILTER (lang(?broaderLg2) = '${LG2}')
    }
}
ORDER BY DESC(xsd:integer(?conceptVersion))
LIMIT 1