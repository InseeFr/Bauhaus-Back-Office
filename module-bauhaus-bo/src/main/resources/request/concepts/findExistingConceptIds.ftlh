SELECT DISTINCT ?id
WHERE {
    VALUES ?id { <#list IDS as id>${id} </#list>}
    ?concept rdf:type skos:Concept .
    FILTER(STRENDS(STR(?concept), CONCAT('/concepts/definition/', ?id))) .
}
