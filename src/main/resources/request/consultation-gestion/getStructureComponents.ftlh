SELECT ?uri ?id ?notation ?prefLabelLg1 ?prefLabelLg2 ?concept ?representation ?listeCode ?ordre ?attachement ?obligatoire
FROM <${STRUCTURES_GRAPH}>
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
WHERE {
    ?uriDSD rdf:type qb:DataStructureDefinition .
    ?uriDSD dcterms:identifier "${STRUCTURE_ID}" .
    ?uriDSD qb:component ?uriComponentSpecification .
    ?uriComponentSpecification (qb:measure|qb:dimension|qb:attribute) ?uri .
    ?uri dcterms:identifier ?id .
    ?uri skos:notation ?notation .
    ?uri rdfs:label ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    ?uri rdfs:label ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .
    OPTIONAL { ?uri qb:concept ?concept . }
    OPTIONAL { ?uri rdfs:range ?representation . }
    OPTIONAL { ?uri qb:codeList ?listeCode . }
    OPTIONAL { ?uriComponentSpecification qb:order ?ordre . }
    OPTIONAL {?uriComponentSpecification qb:componentAttachment ?attachement . }
    OPTIONAL {?uriComponentSpecification qb:componentRequired ?obligatoire . }
}