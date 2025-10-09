package fr.insee.rmes.graphdb.codeslists;


import fr.insee.rmes.sparql.annotations.*;

@Entity(type = "skos:Collection")
@Graph("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}")
public record PartialCodesList(
        @Predicate("skos:notation")
    String id,

        @Statement
    String uri,

        @Predicate("skos:prefLabel")
    @DefaultSortField
    String labelLg1,

        @Predicate(value = "skos:prefLabel", optional = true)
    String labelLg2,

        @Predicate(value = "rdfs:seeAlso", optional = true)
    String range
) {
}