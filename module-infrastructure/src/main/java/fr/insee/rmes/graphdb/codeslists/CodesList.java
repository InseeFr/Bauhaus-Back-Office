package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.graphdb.annotations.Entity;
import fr.insee.rmes.graphdb.annotations.Graph;
import fr.insee.rmes.graphdb.annotations.Predicate;
import fr.insee.rmes.graphdb.annotations.Statement;
import fr.insee.rmes.graphdb.annotations.DefaultSortField;

@Entity(type = "skos:ConceptScheme")
@Graph("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}")
public record CodesList(
    @Predicate(value = "skos:notation")
    String id,
    
    @Statement
    String uri,
    
    @Predicate(value = "skos:prefLabel")
    @DefaultSortField
    String labelLg1,
    
    @Predicate(value = "skos:prefLabel")
    String labelLg2,
    
    @Predicate(value = "rdfs:seeAlso")
    String range
) {
}