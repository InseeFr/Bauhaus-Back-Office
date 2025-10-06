package fr.insee.rmes.graphdb.codeslists;

import fr.insee.rmes.graphdb.annotations.Entity;
import fr.insee.rmes.graphdb.annotations.Graph;
import fr.insee.rmes.graphdb.annotations.Predicate;
import fr.insee.rmes.graphdb.annotations.Statement;
import fr.insee.rmes.graphdb.annotations.DefaultSortField;

import java.util.List;

@Entity(type = "skos:ConceptScheme")
@Graph("${fr.insee.rmes.bauhaus.baseGraph}${fr.insee.rmes.bauhaus.codelists.graph}")
public record CodesList(
    @Statement
    String uri,

    @Predicate(value = "skos:notation")
    String id,

    @Predicate(value = "skos:prefLabel", lang = "lg1")
    @DefaultSortField
    String labelLg1,
    
    @Predicate(value = "skos:prefLabel", optional = true, lang = "lg2")
    String labelLg2,

    @Predicate(value = "skos:definition", optional = true, lang = "lg1")
    String descriptionLg1,

    @Predicate(value = "skos:definition", optional = true, lang = "lg2")
    String descriptionLg2,
    
    @Predicate(value = "rdfs:seeAlso", optional = true, inverse = true)
    String range,

    @Predicate(value = "insee:lastCodeUriSegment", optional = true)
    String lastCodeUriSegment,

    @Predicate(value = "dcterms:created", optional = true)
    String created,

    @Predicate(value = "dc:creator", optional = true)
    String creator,

    @Predicate(value = "insee:validationState", optional = true)
    String validationState,

    @Predicate(value = "insee:disseminationStatus", optional = true)
    String disseminationStatus,

    @Predicate(value = "dcterms:modified", optional = true)
    String modified,

    @Predicate(value = "prov:wasDerivedFrom", optional = true)
    String iriParent,

    @Predicate(value = "dc:contributor", optional = true)
    List<String> contributor
) {
}