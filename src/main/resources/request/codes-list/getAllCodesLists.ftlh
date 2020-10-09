SELECT ?value ?label ?range
WHERE {
{
GRAPH <http://rdf.insee.fr/graphes/codes> {
?value rdf:type skos:ConceptScheme .
?value skos:prefLabel ?label .
FILTER(lang(?label) = 'fr') .
?value rdfs:seeAlso ?range
}
}
UNION
{
?value ?b xkos:ClassificationLevel .
?value skos:prefLabel ?label .
FILTER(lang(?label) = 'fr') .
?value xkos:organizedBy ?range
}
}