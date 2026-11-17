SELECT ?id ?descriptionLg1 ?descriptionLg2 ?descriptionWithoutLanguage
FROM ${DATASET_GRAPH}
WHERE {
    ?uri rdf:type dcat:Dataset ;
         dcterms:identifier ${ID} ;
         prov:wasDerivedFrom ?source .
    ?source rdf:type dcat:Dataset ;
            dcterms:identifier ?id .
    OPTIONAL {
        ?uri prov:qualifiedDerivation ?node .
        OPTIONAL {
            ?node dcterms:description ?descriptionLg1 .
            FILTER (lang(?descriptionLg1) = ${LG1})
        }
        OPTIONAL {
            ?node dcterms:description ?descriptionLg2 .
            FILTER (lang(?descriptionLg2) = ${LG2})
        }
        OPTIONAL {
            ?node dcterms:description ?descriptionWithoutLanguage .
            FILTER (lang(?descriptionWithoutLanguage) = "")
        }
    }
}
