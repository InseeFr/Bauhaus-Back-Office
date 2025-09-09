SELECT ?id ?labelLg1 ?labelLg2 ?parent ?altlabelLg1 ?altlabelLg2
WHERE {
    ?classification rdf:type skos:ConceptScheme .
    FILTER(REGEX(STR(?classification),'/codes/${ID}/')) .
    ?item skos:inScheme ?classification .
    ?item skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')
    OPTIONAL{?item skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') }
    OPTIONAL {
        ?item skos:altLabel ?altlabelLg1 .
        FILTER (lang(?altlabelLg1) = '${LG1}')
    }
    OPTIONAL {
        ?item skos:altLabel ?altlabelLg2 .
        FILTER (lang(?altlabelLg2) = '${LG2}')
    }
    ?item skos:notation ?id .
    OPTIONAL {
        ?item skos:broader ?broader .
        ?broader skos:notation ?parent
    }
    ?level skos:member ?item .
    ?level xkos:depth ?depth
}
ORDER BY ?depth ?id