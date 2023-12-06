SELECT ?id ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?disseminationStatus ?idSerie ?theme
?validationState ?abstractLg1 ?abstractLg2 ?cautionLg1 ?cautionLg2
?catalogRecordCreated ?catalogRecordUpdated ?catalogRecordCreator ?catalogRecordContributor
?updated
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:identifier '${ID}' .
    ?uri dcterms:title ?labelLg1
    FILTER (lang(?labelLg1) = '${LG1}') .

    OPTIONAL {
        ?uri dcterms:title ?labelLg2
        FILTER (lang(?labelLg2) = '${LG2}') .
    }

    OPTIONAL {
        ?uri dcterms:description ?descriptionLg1
        FILTER (lang(?descriptionLg1) = '${LG1}') .
    }

    OPTIONAL {
        ?uri dcterms:description ?descriptionLg2
        FILTER (lang(?descriptionLg2) = '${LG2}') .
    }

    OPTIONAL {
        ?uri dcterms:abstract ?abstractLg1
        FILTER (lang(?abstractLg1) = '${LG1}') .
    }

    OPTIONAL {
        ?uri dcterms:abstract ?abstractLg2
        FILTER (lang(?abstractLg2) = '${LG2}') .
    }

    OPTIONAL {
        ?uri skos:scopeNote ?cautionLg1
        FILTER (lang(?cautionLg1) = '${LG1}') .
    }

    OPTIONAL {
        ?uri skos:scopeNote ?cautionLg2
        FILTER (lang(?cautionLg2) = '${LG2}') .
    }


    OPTIONAL {
        ?catalogRecordUri foaf:primaryTopic ?uri .
    }

    OPTIONAL {
        ?catalogRecordUri dc:creator ?catalogRecordCreator .
    }

    OPTIONAL {
        ?catalogRecordUri dc:contributor ?catalogRecordContributor .
    }

    OPTIONAL {
        ?catalogRecordUri dcterms:created ?catalogRecordCreated .
    }

    OPTIONAL {
        ?catalogRecordUri dcterms:modified ?catalogRecordUpdated .
    }


    OPTIONAL {
        ?uri insee:disseminationStatus ?disseminationStatus .
    }

    OPTIONAL {
        ?uri prov:wasGeneratedBy ?wasGeneratedBy .
        BIND(STRAFTER(STR(?wasGeneratedBy),'/operations/serie/') AS ?idSerie) .
    }

    OPTIONAL {
        ?uri dcat:theme ?theme .
    }

    OPTIONAL {
        ?uri insee:validationState ?validationState .
    }

    OPTIONAL {
        ?uri dcterms:modified ?updated .
    }

    BIND(STRAFTER(STR(?uri),'/datasets/') AS ?id) .
}