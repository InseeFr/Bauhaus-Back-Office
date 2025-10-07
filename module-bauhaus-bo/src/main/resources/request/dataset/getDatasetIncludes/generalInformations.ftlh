?uri dcterms:title ?labelLg1
FILTER (lang(?labelLg1) = '${LG1}') .

OPTIONAL {
?uri dcterms:title ?labelLg2
FILTER (lang(?labelLg2) = '${LG2}') .
}

OPTIONAL {
    ?uri insee:subtitle ?subTitleLg1
    FILTER (lang(?subTitleLg1) = '${LG1}') .
}

OPTIONAL {
    ?uri insee:subtitle ?subTitleLg2
    FILTER (lang(?subTitleLg2) = '${LG2}') .
}

OPTIONAL {
    ?uri dcterms:accrualPeriodicity ?accrualPeriodicity
}
OPTIONAL {
    ?uri dcterms:accessRights ?accessRights
}
OPTIONAL {
    ?uri insee:confidentialityStatus ?confidentialityStatus
}
OPTIONAL {
    ?uri dcterms:publisher ?publisher
}
OPTIONAL {
    ?uri dcat:landingPage ?landingPageLg1
    FILTER (lang(?landingPageLg1) = '${LG1}') .
}

OPTIONAL {
    ?uri dcterms:modified ?updated .
}
OPTIONAL {
    ?uri dcterms:issued ?issued .
}