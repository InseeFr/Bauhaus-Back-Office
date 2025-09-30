package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.graphdb.annotations.Entity;
import fr.insee.rmes.graphdb.annotations.Graph;
import fr.insee.rmes.graphdb.annotations.Predicate;
import fr.insee.rmes.graphdb.annotations.Statement;
import fr.insee.rmes.graphdb.annotations.DefaultSortField;

@Entity(type = "skos:Collection")
@Graph("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}")
public record PartialCodesList(
    @Predicate(value = "skos:notation")
    String id,
    
    @Statement
    String uri,
    
    @Predicate(value = "skos:prefLabel")
    @DefaultSortField
    String labelLg1,
    
    @Predicate(value = "skos:prefLabel", optional = true)
    String labelLg2,
    
    @Predicate(value = "rdfs:seeAlso", optional = true)
    String range
) {
}