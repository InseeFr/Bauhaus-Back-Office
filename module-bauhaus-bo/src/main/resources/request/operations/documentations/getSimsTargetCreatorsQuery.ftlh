<#-- Créateurs de la cible du rapport : la série elle-même, la série parente d'une opération ou
     l'indicateur. Le rapport vit dans son propre graphe, sa cible dans celui des opérations ou des
     produits. -->
SELECT DISTINCT ?creators
FROM ${DOCUMENTATION_GRAPH_IRI}
FROM ${OPERATIONS_GRAPH}
FROM ${PRODUCTS_GRAPH}
WHERE {
    ?report a sdmx-mm:MetadataReport ;
        sdmx-mm:target/dcterms:isPartOf?/dc:creator ?creators .
}
