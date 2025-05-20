SELECT ?labelLg1 ?creator ?disseminationStatus ?validationStatus ?wasGeneratedIRIs ?created ?updated

FROM <${DATASET_GRAPH}>

WHERE {
    ?uri rdf:type dcat:Dataset .

    ?uri dct:title ?labelLg1
    FILTER (lang(?labelLg1) = '${LG1}') .

    ?uri ^foaf:primaryTopic/dc:creator ?creator .

    ?uri insee:disseminationStatus ?disseminationStatus .

    ?uri insee:validationState ?validationStatus .

    ?uri prov:wasGeneratedBy ?wasGeneratedIRIs .

    ?uri ^foaf:primaryTopic/dct:created ?created .

    ?uri ^foaf:primaryTopic/dct:modified ?modified .
}