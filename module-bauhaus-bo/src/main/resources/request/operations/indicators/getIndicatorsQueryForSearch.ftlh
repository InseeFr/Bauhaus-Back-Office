SELECT ?id ?prefLabelLg1 ?prefLabelLg2 (group_concat(?altLabelLang1;separator=' || ') as ?altLabelLg1) ?altLabelLg2  ?abstractLg1 ?abstractLg2
?historyNoteLg1 ?historyNoteLg2  ?accrualPeriodicityCode ?accrualPeriodicityList  ?publishers  ?idSims  ?validationState
WHERE {
?indic a insee:StatisticalIndicator .
BIND(STRAFTER(STR(?indic),'/${PRODUCT_BASE_URI}/') AS ?id) .
?indic skos:prefLabel ?prefLabelLg1
FILTER (lang(?prefLabelLg1) = '${LG1}')
OPTIONAL{?indic skos:prefLabel ?prefLabelLg2
FILTER (lang(?prefLabelLg2) = '${LG2}') }
OPTIONAL{?indic skos:altLabel ?altLabelLang1
FILTER (lang(?altLabelLang1) = '${LG1}') }
OPTIONAL{?indic skos:altLabel ?altLabelLg2
FILTER (lang(?altLabelLg2) = '${LG2}') }
OPTIONAL{?indic dcterms:abstract ?abstractLg1
FILTER (lang(?abstractLg1) = '${LG1}') }
OPTIONAL{?indic dcterms:abstract ?abstractLg2
FILTER (lang(?abstractLg2) = '${LG2}') }
OPTIONAL{?indic skos:historyNote ?historyNoteLg1
FILTER (lang(?historyNoteLg1) = '${LG1}') }
OPTIONAL{?indic skos:historyNote ?historyNoteLg2
FILTER (lang(?historyNoteLg2) = '${LG2}') }
OPTIONAL {?indic dcterms:accrualPeriodicity ?accrualPeriodicity .
?accrualPeriodicity skos:notation ?accrualPeriodicityCode .
?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList .
?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList .
}
OPTIONAL {?indic dcterms:publisher ?uriPublisher .
?uriPublisher dcterms:identifier  ?publishers .
}
OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport . ?report sdmx-mm:target ?indic  BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
}
OPTIONAL {?indic insee:validationState ?validationState .
}
}
GROUP BY ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLang1 ?altLabelLg2 ?abstractLg1 ?abstractLg2 ?historyNoteLg1 ?historyNoteLg2
?accrualPeriodicityCode ?accrualPeriodicityList  ?publishers  ?idSims  ?validationState
