SELECT ?externalSource ?otherDescription
FROM ${DATASET_GRAPH}
WHERE {
    ?uri rdf:type dcat:Dataset ;
         dcterms:identifier ${ID} .
    {
        ?uri prov:wasDerivedFrom ?externalSource .
        FILTER NOT EXISTS {
            ?externalSource rdf:type dcat:Dataset ;
                            dcterms:identifier ?anyIdentifier .
        }
    }
    UNION
    {
        ?uri prov:qualifiedDerivation ?node .
        ?node dcterms:description ?otherDescription .
        FILTER (lang(?otherDescription) != "" && lang(?otherDescription) != ${LG1} && lang(?otherDescription) != ${LG2})
    }
}
