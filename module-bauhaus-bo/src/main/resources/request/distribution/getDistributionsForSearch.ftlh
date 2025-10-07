SELECT ?distributionId ?distributionLabelLg1 ?distributionValidationStatus ?distributionCreated ?distributionUpdated ?altIdentifier
?id ?labelLg1 ?creator ?disseminationStatus ?validationStatus ?wasGeneratedIRIs ?created ?updated

FROM <${DATASET_GRAPH}>
FROM <${ADMS_GRAPH}>

WHERE {
    ?uri rdf:type dcat:Distribution .

    ?uriDataset rdf:type dcat:Dataset .
    ?uriDataset dcat:distribution ?uri .

    ?uri dcterms:identifier ?distributionId .

    OPTIONAL {
        ?uri dcterms:title ?distributionLabelLg1
        FILTER (lang(?distributionLabelLg1) = '${LG1}') .
    }

    OPTIONAL {
        ?uri insee:validationState ?distributionValidationStatus .
    }

    OPTIONAL {
        ?uri dcterms:created ?distributionCreated .
    }

    OPTIONAL {
        ?uri dcterms:modified ?distributionUpdated .
    }

    OPTIONAL {
        ?uriDataset adms:identifier/skos:notation ?altIdentifier .
    }

    OPTIONAL {
        ?uriDataset dcterms:identifier ?id .
    }

    OPTIONAL {
        ?uriDataset dcterms:title ?labelLg1
        FILTER (lang(?labelLg1) = '${LG1}') .
    }

    OPTIONAL {
        ?uriDataset ^foaf:primaryTopic/dc:creator ?creator .
    }

    OPTIONAL {
        ?uriDataset insee:disseminationStatus ?disseminationStatus .
    }

    OPTIONAL {
        ?uriDataset insee:validationState ?validationStatus .
    }

    OPTIONAL {
        ?uriDataset prov:wasGeneratedBy ?wasGeneratedIRIs .
    }

    OPTIONAL {
        ?uriDataset ^foaf:primaryTopic/dcterms:created ?created .
    }

    OPTIONAL {
        ?uriDataset ^foaf:primaryTopic/dcterms:modified ?modified .
    }
}