SELECT ?id ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2
?idFirstClass ?firstClassLabelLg1 ?firstClassLabelLg2 ?firstAltLabelLg1 ?firstAltLabelLg2
?idSecondClass ?secondClassLabelLg1 ?secondClassLabelLg2 ?secondAltLabelLg1 ?secondAltLabelLg2
WHERE {
    ?correspondence rdf:type xkos:Correspondence .
    FILTER(STRENDS(STR(?correspondence),${CORRESPONDENCE_URI_SUFFIX})) .
    BIND(STRAFTER(STR(?correspondence),'/codes/') AS ?id)
    ?correspondence skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = ${LG1})
    OPTIONAL {?correspondence skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = ${LG2}) } .
    OPTIONAL {?correspondence skos:description ?descriptionLg1 .
    FILTER (lang(?descriptionLg1) = ${LG1}) } .
    OPTIONAL {?correspondence dc:description ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = ${LG2}) } .

    ?correspondence xkos:compares ?firstClassURI .
    FILTER(REGEX(STR(?firstClassURI),${FIRST_CLASS_URI_PATTERN})) .
    BIND(${FIRST_ID} as ?idFirstClass) .
    ?firstClassURI skos:prefLabel ?firstClassLabelLg1 .
    FILTER (lang(?firstClassLabelLg1) = ${LG1})  .
    OPTIONAL {?firstClassURI skos:prefLabel ?firstClassLabelLg2 .
    FILTER (lang(?firstClassLabelLg2) = ${LG2}) }  .
    OPTIONAL {?firstClassURI skos:altLabel ?firstAltLabelLg1 .
    FILTER (lang(?firstAltLabelLg1) = ${LG1}) } .
    OPTIONAL {?firstClassURI skos:altLabel ?firstAltLabelLg2 .
    FILTER (lang(?firstAltLabelLg2) = ${LG2}) } .

    ?correspondence xkos:compares ?secondClassURI .
    FILTER(REGEX(STR(?secondClassURI),${SECOND_CLASS_URI_PATTERN})) .
    BIND(${SECOND_ID} as ?idSecondClass) .
    ?secondClassURI skos:prefLabel ?secondClassLabelLg1 .
    FILTER (lang(?secondClassLabelLg1) = ${LG1})  .
    OPTIONAL {?secondClassURI skos:prefLabel ?secondClassLabelLg2 .
    FILTER (lang(?secondClassLabelLg2) = ${LG2}) }  .
    OPTIONAL {?secondClassURI skos:altLabel ?secondAltLabelLg1 .
    FILTER (lang(?secondAltLabelLg1) = ${LG1}) } .
    OPTIONAL {?secondClassURI skos:altLabel ?secondAltLabelLg2 .
    FILTER (lang(?secondAltLabelLg2) = ${LG2}) } .
}
LIMIT 1
