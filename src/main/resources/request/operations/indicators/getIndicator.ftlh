SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?created ?modified ?altLabelLg1 ?altLabelLg2  ?abstractLg1 ?abstractLg2  ?historyNoteLg1 ?historyNoteLg2  ?accrualPeriodicityCode ?accrualPeriodicityList  ?publishers ?creators  ?idSims  ?validationState  WHERE {
FILTER(STRENDS(STR(?indic),'/produits/indicateur/${ID}')) .
OPTIONAL { ?indic dcterms:created ?created } .
OPTIONAL { ?indic dcterms:modified ?modified } .
?indic skos:prefLabel ?prefLabelLg1
FILTER (lang(?prefLabelLg1) = '${LG1}')
OPTIONAL{?indic skos:prefLabel ?prefLabelLg2
FILTER (lang(?prefLabelLg2) = '${LG2}') }
OPTIONAL{?indic skos:altLabel ?altLabelLg1
FILTER (lang(?altLabelLg1) = '${LG1}') }
OPTIONAL{?indic skos:altLabel ?altLabelLg2
FILTER (lang(?altLabelLg2) = '${LG2}') }

OPTIONAL {
    ?indic dcterms:abstract ?abstractLg1Uri .
    ?abstractLg1Uri evoc:noteLiteral ?abstractLg1 .
    ?abstractLg1Uri xsd:language '${LG1}'^^xsd:language
}

OPTIONAL {
    ?indic dcterms:abstract ?abstractLg2Uri .
    ?abstractLg2Uri evoc:noteLiteral ?abstractLg2 .
    ?abstractLg2Uri xsd:language '${LG2}'^^xsd:language
}

OPTIONAL {
    ?indic skos:historyNote ?historyNoteLg1Uri .
    ?historyNoteLg1Uri evoc:noteLiteral ?historyNoteLg1 .
    ?historyNoteLg1Uri xsd:language '${LG1}'^^xsd:language
}

OPTIONAL {
    ?indic skos:historyNote ?historyNoteLg2Uri .
    ?historyNoteLg2Uri evoc:noteLiteral ?historyNoteLg2 .
    ?historyNoteLg2Uri xsd:language '${LG2}'^^xsd:language
}

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
OPTIONAL {?indic dc:creator ?creators .
}
OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport . ?report sdmx-mm:target ?indic  BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
}
OPTIONAL {?indic insee:validationState ?validationState .
}
}

<#if WITH_LIMIT >
    LIMIT 1
</#if>

