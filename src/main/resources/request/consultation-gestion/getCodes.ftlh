SELECT ?uri ?code ?prefLabelLg1 ?prefLabelLg2 ?parents ?ordre
FROM <${CODELIST_GRAPH}>
WHERE {
    {
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
        OPTIONAL {
            ?uriListe ?predicatRdfSeq ?uri .
            filter(contains(str(?predicatRdfSeq), "http://www.w3.org/1999/02/22-rdf-syntax-ns#_" )) .
            bind(strafter(str(?predicatRdfSeq),"http://www.w3.org/1999/02/22-rdf-syntax-ns#_") as ?ordre) .
        }
    }
    UNION
    {
        ?uriListe rdf:type skos:Collection .
        ?uriListe skos:notation "${NOTATION}" .
        ?uriListe skos:member ?uri .
        ?uri skos:notation ?code .
        ?uri skos:prefLabel ?prefLabelLg1 .
        FILTER (lang(?prefLabelLg1) = '${LG1}') .
        ?uri skos:prefLabel ?prefLabelLg2 .
        FILTER (lang(?prefLabelLg2) = '${LG2}') .
        ?uri rdf:type skos:Concept .
        OPTIONAL {
            ?uri skos:broader ?parentsParent .
            ?parentsParent skos:notation ?parents
        } .
    }
}