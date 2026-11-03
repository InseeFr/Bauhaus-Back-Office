SELECT ?id
       (SAMPLE(?labelLg1Value) AS ?labelLg1)
       (SAMPLE(?creatorValue) AS ?creator)
       (SAMPLE(?disseminationStatusValue) AS ?disseminationStatus)
       (SAMPLE(?validationStatusValue) AS ?validationStatus)
       (GROUP_CONCAT(DISTINCT ?wasGeneratedIRI; separator=" ") AS ?wasGeneratedIRIs)
       (SAMPLE(?createdValue) AS ?created)
       (SAMPLE(?updatedValue) AS ?updated)
       (SAMPLE(?altIdentifierValue) AS ?altIdentifier)

FROM ${DATASET_GRAPH}
FROM ${ADMS_GRAPH}

WHERE {
    ?uri rdf:type dcat:Dataset .

    ?uri dcterms:identifier ?id .

    OPTIONAL {
        ?uri dcterms:title ?labelLg1Value
        FILTER (lang(?labelLg1Value) = ${LG1}) .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dc:creator ?creatorValue .
    }

    OPTIONAL {
        ?uri insee:disseminationStatus ?disseminationStatusValue .
    }

    OPTIONAL {
        ?uri insee:validationState ?validationStatusValue .
    }

    OPTIONAL {
        ?uri prov:wasGeneratedBy ?wasGeneratedIRI .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dcterms:created ?createdValue .
    }

    OPTIONAL {
        ?uri ^foaf:primaryTopic/dcterms:modified ?updatedValue .
    }

    OPTIONAL {
        ?uri adms:identifier ?admsIdentifierUri .
        ?admsIdentifierUri rdf:type adms:Identifier .
        ?admsIdentifierUri skos:notation ?altIdentifierValue .
    }
}
GROUP BY ?id
