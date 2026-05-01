SELECT DISTINCT ?id
WHERE {
    VALUES ?id { <#list IDS as id>"${id}" </#list>}
    ?collection rdf:type skos:Collection .
    FILTER(STRENDS(STR(?collection), CONCAT('/concepts/definitions/', ?id))) .
}
