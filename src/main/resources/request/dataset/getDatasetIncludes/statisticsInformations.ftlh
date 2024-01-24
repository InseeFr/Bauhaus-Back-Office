OPTIONAL {
    ?uri dcterms:type ?type .
}
OPTIONAL {
    ?uri insee:statisticalUnit ?statisticalUnit .
}

OPTIONAL {
    ?uri insee:structure ?dataStructure .
}

OPTIONAL {
    ?uri insee:numObservations ?observationNumber .
}

OPTIONAL {
    ?uri dcterms:spatial ?spacialCoverage .
}

OPTIONAL {
    ?uri dcat:temporalResolution ?temporalResolution.
}
