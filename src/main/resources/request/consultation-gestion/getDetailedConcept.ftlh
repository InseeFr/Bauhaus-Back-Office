SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?namingHint ?created ?modified ?valid ?isValidated
FROM <${CONCEPTS_GRAPH}>
WHERE {
    ?concept skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    FILTER(REGEX(STR(?concept),'/concepts/definition/${ID}')) .
    BIND(STRAFTER(STR(?concept),'/definition/') AS ?id) .
    ?concept skos:prefLabel ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .
    OPTIONAL {
        ?concept skos:closeMatch ?conceptSDMX .
        FILTER(strStarts(STR(?conceptSDMX),'urn:sdmx:org.sdmx.infomodel.conceptscheme.Concept'))
        BIND(STRAFTER(STR(?conceptSDMX),').') AS ?namingHint) .
    } .
    ?concept dcterms:created ?created .
    ?concept dcterms:modified ?modified .
    ?concept insee:isValidated ?isValidated .
    OPTIONAL { ?concept dcterms:valid ?valid . } .
} LIMIT 1