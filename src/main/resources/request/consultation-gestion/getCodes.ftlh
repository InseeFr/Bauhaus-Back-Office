SELECT ?uri ?code ?prefLabelLg1 ?prefLabelLg2 ?parents
FROM <${CODELIST_GRAPH}>
WHERE {
    ?uriListe rdf:type skos:ConceptScheme .
    ?uriListe skos:notation "${NOTATION}" .
    ?uri rdf:type skos:Concept .
    ?uri skos:inScheme ?uriListe .
    ?uri skos:notation ?code .
    ?uri skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    ?uri skos:prefLabel ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .
    OPTIONAL {
        ?uri skos:broader ?parentsParent .
        ?parentsParent skos:notation ?parents
    } .
}