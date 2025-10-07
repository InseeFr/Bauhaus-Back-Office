SELECT DISTINCT ?id
FROM <${DATASET_GRAPH}>
WHERE {
    ?uri a dcat:Dataset ;
         dcterms:identifier ?id .
    ?uri prov:wasDerivedFrom ?wasDerivedFrom .
    ?wasDerivedFrom dcterms:identifier '${ID}'  .
}
GROUP BY ?id