SELECT ?id ?label
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:identifier ?id .
    ?uri dcterms:title ?label
    FILTER (lang(?label) = '${LG1}') .


<#if STAMP??>
    ?catalogRecordUri foaf:primaryTopic ?uri .
    ?catalogRecordUri dc:contributor '${STAMP}' .
</#if>
}