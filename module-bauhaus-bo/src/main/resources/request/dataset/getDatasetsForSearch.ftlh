SELECT ?id
       (SAMPLE(?labelLg1) AS ?labelLg1)
       (SAMPLE(?creator) AS ?creator)
       (SAMPLE(?disseminationStatus) AS ?disseminationStatus)
       (SAMPLE(?validationStatus) AS ?validationStatus)
       (GROUP_CONCAT(DISTINCT ?wasGeneratedIRI; separator=" ") AS ?wasGeneratedIRIs)
       (SAMPLE(?created) AS ?created)
       (SAMPLE(?updated) AS ?updated)
       (SAMPLE(?altIdentifier) AS ?altIdentifier)

FROM ${DATASET_GRAPH}
FROM ${ADMS_GRAPH}

WHERE {
    ?uri rdf:type dcat:Dataset .

    ?uri dcterms:identifier ?id .

    OPTIONAL {
        ?uri dcterms:title ?labelLg1
        FILTER (lang(?labelLg1) = ${LG1}) .
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
        ?uri prov:wasGeneratedBy ?wasGeneratedIRI .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dcterms:created ?created .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dcterms:modified ?updated .
    }

    OPTIONAL {
        ?uri adms:identifier ?admsIdentifierUri .
        ?admsIdentifierUri rdf:type adms:Identifier .
        ?admsIdentifierUri skos:notation ?altIdentifier .
    }
}
GROUP BY ?id
