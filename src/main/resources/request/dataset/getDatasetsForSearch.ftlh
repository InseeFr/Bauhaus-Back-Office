SELECT ?id ?labelLg1 ?creator ?disseminationStatus ?validationStatus ?wasGeneratedIRIs ?created ?updated

FROM <${DATASET_GRAPH}>

WHERE {
    ?uri rdf:type dcat:Dataset .

    ?uri dcterms:identifier ?id .

    OPTIONAL {
        ?uri dcterms:title ?labelLg1
        FILTER (lang(?labelLg1) = '${LG1}') .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dc:creator ?creator .
    }

    OPTIONAL {
        ?uri insee:disseminationStatus ?disseminationStatus .
    }

    OPTIONAL {
        ?uri insee:validationState ?validationStatus .
    }

    OPTIONAL {
        ?uri prov:wasGeneratedBy ?wasGeneratedIRIs .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dcterms:created ?created .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dcterms:modified ?updated .
    }
}