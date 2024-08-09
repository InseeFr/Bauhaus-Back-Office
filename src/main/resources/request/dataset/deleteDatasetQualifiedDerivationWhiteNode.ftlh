DELETE WHERE {
GRAPH <http://rdf.insee.fr/graphes/catalogue>
    {
?uri rdf:type dcat:Dataset .
    ?uri prov:qualifiedDerivation ?node .
    ?uri dcterms:identifier '${ID}' .
    ?node rdf:type prov:Derivation .
    }
}