SELECT ?id ?iri
FROM ${DATASET_GRAPH}
WHERE {
    VALUES ?id { <#list IDS as id>${id} </#list>}
    ?iri rdf:type dcat:Dataset ;
         dcterms:identifier ?id .
}
