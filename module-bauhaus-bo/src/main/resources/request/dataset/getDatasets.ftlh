# DISTINCT : un jeu de données peut exister sous plusieurs IRI pour un même
# dct:identifier (cf. les mises à jour écrites sous l'IRI dérivée de la
# configuration). La liste doit rester à une ligne par identifiant.
SELECT DISTINCT ?id ?label
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri rdf:type dcat:Dataset .
    ?uri dcterms:identifier ?id .
    ?uri dcterms:title ?label
    FILTER (lang(?label) = '${LG1}') .


<#if STAMP??>
    ?catalogRecordUri foaf:primaryTopic ?uri .
    ?catalogRecordUri dc:contributor ?contributor .
    VALUES ?contributor { <#list STAMP as s>'${s}' </#list>}
</#if>
}