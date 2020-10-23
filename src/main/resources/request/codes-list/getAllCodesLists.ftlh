SELECT ?uri ?label ?range
WHERE {
{
GRAPH <${CODES_LISTS_GRAPH}> {
?uri rdf:type skos:ConceptScheme .
?uri skos:prefLabel ?label .
FILTER(lang(?label) = 'fr') .
?range rdfs:seeAlso ?uri
}
}
UNION
{
?uri rdf:type xkos:ClassificationLevel .
?uri skos:prefLabel ?label .
FILTER(lang(?label) = 'fr') .
?uri xkos:organizedBy ?range
}
}