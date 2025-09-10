SELECT DISTINCT
?id
?uri
(GROUP_CONCAT(DISTINCT ?wasDerivedFromS; separator=" , ") AS ?wasDerivedFromS)

FROM <http://rdf.insee.fr/graphes/catalogue>

WHERE {
    ?uri a dcat:Dataset ;
         dcterms:identifier '${ID}' ;
         dcterms:identifier ?id ;

    OPTIONAL {?uri prov:wasDerivedFrom ?wasDerivedFrom .
              ?wasDerivedFrom dcterms:identifier ?wasDerivedFromS
             } .


}
GROUP BY ?id ?uri


