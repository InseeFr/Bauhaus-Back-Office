SELECT DISTINCT ?attributeId ?attributeValueIri ?attributeValueCode
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${CODELIST_GRAPH}>
WHERE {
    ?component dcterms:identifier '${ID}' ;
    skos:notation ?identifiant ;
    ?predicate ?attributeValueIri .
    ?attributeValueIri skos:notation ?attributeValueCode .
    ?predicate dcterms:identifier ?attributeId .
    FILTER(STRSTARTS(STR(?predicate), "http://bauhaus/structuresDeDonnees/composants/attribut/"))
}