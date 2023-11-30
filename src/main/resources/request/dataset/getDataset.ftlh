SELECT ?id ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?creator ?contributor ?disseminationStatus ?idSerie ?theme
?validationState ?created ?updated ?abstractLg1 ?abstractLg2 ?cautionLg1 ?cautionLg2
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
        ?uri dcterms:creator ?creator .
    }

    OPTIONAL {
        ?uri dcterms:contributor ?contributor .
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
    ?uri dcterms:created ?created .
    }

    OPTIONAL {
    ?uri dcterms:modified ?updated .
    }

    OPTIONAL {
    ?uri insee:validationState ?validationState .
    }

    BIND(STRAFTER(STR(?uri),'/catalogues/jeuDeDonnees/') AS ?id) .
}