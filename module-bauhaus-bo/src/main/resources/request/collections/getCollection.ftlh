SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?created ?modified ?descriptionLg1 ?descriptionLg2
?validationState ?creator ?contributor
WHERE {
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection),${COLLECTION_URI_SUFFIX})) .
    BIND(STRAFTER(STR(?collection),'/concepts/definitions/') AS ?id) .

    ?collection dcterms:title ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = ${LG1}) .

    OPTIONAL {?collection dcterms:title ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = ${LG2})} .

    ?collection dcterms:created ?created .
    OPTIONAL {?collection dcterms:modified ?modified} .

    OPTIONAL {?collection dcterms:description ?descriptionLg1 .
    FILTER (lang(?descriptionLg1) = ${LG1}) }
    OPTIONAL {?collection dcterms:description ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = ${LG2}) } .

    ?collection insee:validationState ?validationState
    OPTIONAL {?collection dc:creator ?creator} .
    ?collection dc:contributor ?contributor .
}