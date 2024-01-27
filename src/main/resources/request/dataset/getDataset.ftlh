SELECT ?id ?labelLg1 ?labelLg2 ?subTitleLg1 ?subTitleLg2 ?accrualPeriodicity ?accessRights ?confidentialityStatus ?publisher ?landingPageLg1 ?landingPageLg2
?updated ?issued
?disseminationStatus ?processStep ?archiveUnit
?type ?dataStructure ?timeSeriesNumber ?observationNumber ?spacialCoverage ?temporalResolution
?temporalCoverageStartDate ?temporalCoverageEndDate (datatype(?temporalCoverageEndDate) AS ?temporalCoverageDataType)
?descriptionLg1 ?descriptionLg2  ?idSerie ?theme
?validationState ?abstractLg1 ?abstractLg2 ?cautionLg1 ?cautionLg2
?catalogRecordCreated ?catalogRecordUpdated ?catalogRecordCreator ?catalogRecordContributor

FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:identifier '${ID}' .

    <#include "getDatasetIncludes/generalInformations.ftlh">

    OPTIONAL {
        ?uri dcat:landingPage ?landingPageLg2
        FILTER (lang(?landingPageLg2) = '${LG2}') .
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
        ?catalogRecordUri foaf:primaryTopic ?uri .
    }

    OPTIONAL {
        ?catalogRecordUri dc:creator ?catalogRecordCreator .
    }

    OPTIONAL {
        ?catalogRecordUri dc:contributor ?catalogRecordContributor .
    }

    OPTIONAL {
        ?catalogRecordUri dcterms:created ?catalogRecordCreated .
    }

    OPTIONAL {
        ?catalogRecordUri dcterms:modified ?catalogRecordUpdated .
    }


<#include "getDatasetIncludes/generalManagment.ftlh">
<#include "getDatasetIncludes/statisticsInformations.ftlh">

    OPTIONAL {
        ?uri prov:wasGeneratedBy ?wasGeneratedBy .
        BIND(STRAFTER(STR(?wasGeneratedBy),'/operations/serie/') AS ?idSerie) .
    }

    OPTIONAL {
        ?uri dcat:theme ?theme .
    }

    OPTIONAL {
        ?uri insee:validationState ?validationState .
    }

    BIND(STRAFTER(STR(?uri),'/datasets/') AS ?id) .
}