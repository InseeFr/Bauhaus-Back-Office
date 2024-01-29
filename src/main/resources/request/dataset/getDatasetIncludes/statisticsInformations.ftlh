OPTIONAL {
    ?uri dcterms:type ?type .
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

OPTIONAL {
    ?uri dcterms:temporal ?blankNode .
    ?blankNode rdf:type dcterms:PeriodOfTime .
    ?blankNode dcat:startDate ?temporalCoverageStartDate .
    ?blankNode dcat:endDate ?temporalCoverageEndDate .
}


OPTIONAL {
    ?uri <http://data.europa.eu/m8g/numSeries> ?timeSeriesNumber.
}
