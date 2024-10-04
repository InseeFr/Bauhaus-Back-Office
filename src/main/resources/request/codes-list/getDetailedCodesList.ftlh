SELECT DISTINCT ?iri ?id ?labelLg1 ?labelLg2 ?created ?modified ?creator ?contributor ?validationState ?disseminationStatus
?descriptionLg1 ?descriptionLg2 ?lastListUriSegment ?lastClassUriSegment ?iriParent ?lastCodeUriSegment
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?iri skos:notation '${NOTATION}' ;
    skos:notation ?id .
    OPTIONAL {
        ?iri insee:lastCodeUriSegment ?lastCodeUriSegment .
    }
    OPTIONAL {
    ?iri dcterms:created ?created .
    }
    OPTIONAL {
    ?iri dc:creator ?creator .
    }
    OPTIONAL {
    ?iri dc:contributor ?contributor .
    }
    OPTIONAL {
    ?iri insee:validationState ?validationState .
    }
    OPTIONAL {
    ?iri insee:disseminationStatus ?disseminationStatus .
    }
    OPTIONAL {
        ?iri dcterms:modified ?modified .
    }

    OPTIONAL {
        ?iri rdfs:seeAlso ?seeAlso .
        BIND(STRAFTER(STR(?seeAlso),'concept/') AS ?lastClassUriSegment) .
    }

    OPTIONAL {
        ?iri prov:wasDerivedFrom ?iriParent .
    } .
    BIND(STRAFTER(STR(?iri),'${CODE_LIST_BASE_URI}/') AS ?lastListUriSegment) .

    ?iri skos:prefLabel ?labelLg1 .
    FILTER(lang(?labelLg1) = '${LG1}') .
    OPTIONAL {?iri skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .
    OPTIONAL {?iri skos:definition ?descriptionLg1 .
    FILTER(lang(?descriptionLg1) = '${LG1}') } .
    OPTIONAL {?iri skos:definition ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .
}
ORDER BY ?labelLg1