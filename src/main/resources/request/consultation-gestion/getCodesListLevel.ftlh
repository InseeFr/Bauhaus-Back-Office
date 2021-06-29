SELECT ?uriNiveau ?idNiveau ?prefLabelLg1 ?prefLabelLg2 ?idNiveauSuivant
FROM <${CODELIST_GRAPH}>
WHERE {
    ?uriListe rdf:type skos:ConceptScheme .
    ?uriListe skos:notation "${NOTATION}" .
    ?uriListe xkos:levels/(rdf:rest)*/rdf:first ?uriNiveau .
    ?uriNiveau rdf:type xkos:ClassificationLevel .
    ?uriNiveau skos:notation ?idNiveau .
    OPTIONAL{
    ?uriNiveau skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .
    }
    OPTIONAL{
    ?uriNiveau skos:prefLabel ?prefLabelLg2 .
    FILTER (lang(?prefLabelLg2) = '${LG2}') .
    }
    OPTIONAL {
    ?uriNiveau ^rdf:first/rdf:rest/rdf:first ?uriNiveauSuivant .
    ?uriNiveauSuivant skos:notation ?idNiveauSuivant .
    }
}