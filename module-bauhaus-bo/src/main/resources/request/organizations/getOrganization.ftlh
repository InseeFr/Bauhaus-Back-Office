SELECT ?labelLg1 ?labelLg2 ?altLabel ?type ?motherOrganization ?linkedTo ?seeAlso
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
    ?organization adms:identifier '${IDENTIFIER}' .

    OPTIONAL { ?organization skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')}
    OPTIONAL {?organization skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') }
    OPTIONAL {?organization skos:altLabel ?altLabel .}

    OPTIONAL {?organization rdf:type ?type .
    FILTER (!strstarts(str(?type),str(org:))) }

    OPTIONAL {?organization org:unitOf ?motherOrganizationUri .
    ?motherOrganizationUri dcterms:identifier ?motherOrganization .}
    OPTIONAL {?organization org:linkedTo ?linkedToUri .
    ?linkedToUri dcterms:identifier ?linkedTo .}

    OPTIONAL {?organization rdfs:seeAlso ?seeAlso .}
}