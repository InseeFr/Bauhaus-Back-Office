SELECT ('${ID}' AS ?id) ?prefLabelLg1 ?prefLabelLg2 ?created ?modified ?altLabelLg1 ?altLabelLg2  ?abstractLg1 ?abstractLg2
?historyNoteLg1 ?historyNoteLg2  ?idSims  ?typeCode ?typeList  ?accrualPeriodicityCode ?accrualPeriodicityList  ?validationState
WHERE {
    FILTER(STRENDS(STR(?series),'/operations/serie/${ID}')) .
    ?series skos:prefLabel ?prefLabelLg1
    OPTIONAL { ?series dcterms:created ?created } .

    OPTIONAL { ?series dcterms:modified ?modified } .
    FILTER (lang(?prefLabelLg1) = '${LG1}')
    OPTIONAL{?series skos:prefLabel ?prefLabelLg2
    FILTER (lang(?prefLabelLg2) = '${LG2}') }
    OPTIONAL{?series skos:altLabel ?altLabelLg1
    FILTER (lang(?altLabelLg1) = '${LG1}') }
    OPTIONAL{?series skos:altLabel ?altLabelLg2
    FILTER (lang(?altLabelLg2) = '${LG2}') }

<#if !SERIES_RICH_TEXT_NEXT_STRUCTURE>

    OPTIONAL {
        ?series dcterms:abstract ?abstractLg1Uri .
        ?abstractLg1Uri evoc:noteLiteral ?abstractLg1 .
        ?abstractLg1Uri xsd:language '${LG1}'^^xsd:language
    }

    OPTIONAL {
        ?series dcterms:abstract ?abstractLg2Uri .
        ?abstractLg2Uri evoc:noteLiteral ?abstractLg2 .
        ?abstractLg2Uri xsd:language '${LG2}'^^xsd:language
    }

    OPTIONAL {
        ?series skos:historyNote ?historyNoteLg1Uri .
        ?historyNoteLg1Uri evoc:noteLiteral ?historyNoteLg1 .
        ?historyNoteLg1Uri xsd:language '${LG1}'^^xsd:language
    }

    OPTIONAL {
        ?series skos:historyNote ?historyNoteLg2Uri .
        ?historyNoteLg2Uri evoc:noteLiteral ?historyNoteLg2 .
        ?historyNoteLg2Uri xsd:language '${LG2}'^^xsd:language
    }
</#if>

    OPTIONAL{?series dcterms:abstract ?abstractLg1
    FILTER (lang(?abstractLg1) = '${LG1}') }
    OPTIONAL{?series dcterms:abstract ?abstractLg2
    FILTER (lang(?abstractLg2) = '${LG2}') }
    OPTIONAL{?series skos:historyNote ?historyNoteLg1
    FILTER (lang(?historyNoteLg1) = '${LG1}') }
    OPTIONAL{?series skos:historyNote ?historyNoteLg2
    FILTER (lang(?historyNoteLg2) = '${LG2}') }

    OPTIONAL {
        ?family dcterms:abstract ?abstractLg1Uri .
        ?abstractLg1Uri xkos:ExplanatoryNote ?abstractLg1
        FILTER (lang(?abstractLg1) = '${LG1}')
    }
    OPTIONAL {
        ?family dcterms:abstract ?abstractLg2Uri .
        ?abstractLg2Uri xkos:ExplanatoryNote ?abstractLg2
        FILTER (lang(?abstractLg2) = '${LG2}')
    }
    OPTIONAL{ ?report rdf:type sdmx-mm:MetadataReport . ?report sdmx-mm:target ?series  BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) .
    }
    OPTIONAL {
        ?series dcterms:type ?type .
        ?type skos:notation ?typeCode .
        ?type skos:inScheme ?typeCodeList .
        ?typeCodeList skos:notation ?typeList .
    }
    OPTIONAL {
        ?series dcterms:accrualPeriodicity ?accrualPeriodicity .
        ?accrualPeriodicity skos:notation ?accrualPeriodicityCode .
        ?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList .
        ?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList .
    }
    OPTIONAL {?series insee:validationState ?validationState .
    }
}
LIMIT 1