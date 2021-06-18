SELECT DISTINCT ?id ?labelLg1 ?labelLg2 ?created ?modified ?creator ?contributor ?validationState ?disseminationStatus ?descriptionLg1 ?descriptionLg2 ?lastListUriSegment ?lastClassUriSegment
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?list skos:notation '${NOTATION}' ;
    skos:notation ?id .

    OPTIONAL {
    ?list dcterms:created ?created .
    }
    OPTIONAL {
    ?list dc:creator ?creator .
    }
    OPTIONAL {
    ?list dc:contributor ?contributor .
    }
    OPTIONAL {
    ?list insee:validationState ?validationState .
    }
    OPTIONAL {
    ?list insee:disseminationStatus ?disseminationStatus .
    }
    OPTIONAL {
    ?list dc:contributor ?contributor .
    }
    OPTIONAL {
        ?list dcterms:modified ?modified .
    }

    OPTIONAL {
        ?list rdfs:seeAlso ?seeAlso .
        BIND(STRAFTER(STR(?seeAlso),'concept/') AS ?lastClassUriSegment) .
    }

    BIND(STRAFTER(STR(?list),'${CODE_LIST_BASE_URI}/') AS ?lastListUriSegment) .

    ?list skos:prefLabel ?labelLg1 .
    FILTER(lang(?labelLg1) = '${LG1}') .
    OPTIONAL {?list skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .

    OPTIONAL { ?list dc:contributor ?contributor } .
    OPTIONAL {?list skos:definition ?descriptionLg1 .
    FILTER(lang(?descriptionLg1) = '${LG1}') } .
    OPTIONAL {?list skos:definition ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .
}
ORDER BY ?labelLg1