SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?created ?modified ?descriptionLg1 ?descriptionLg2
?isValidated ?creator ?contributor
WHERE {
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection),'/concepts/definitions/${ID}')) .
    BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) .

    ?collection dcterms:title ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .

    OPTIONAL {?collection dcterms:title ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}')} .

    ?collection dcterms:created ?created .
    OPTIONAL {?collection dcterms:modified ?modified} .

    OPTIONAL {?collection dcterms:description ?descriptionLg1 .
    FILTER (lang(?descriptionLg1) = '${LG1}') }
    OPTIONAL {?collection dcterms:description ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .

    ?collection insee:isValidated ?isValidated
    OPTIONAL {?collection dc:creator ?creator} .
    ?collection dc:contributor ?contributor .
}