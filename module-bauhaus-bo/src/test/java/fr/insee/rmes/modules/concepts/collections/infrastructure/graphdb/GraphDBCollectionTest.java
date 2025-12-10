package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.modules.concepts.collections.domain.model.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionId;
import fr.insee.rmes.modules.concepts.concept.domain.model.ConceptId;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphDBCollectionTest {

    @Test
    void should_convert_from_domain_with_all_fields() {
        // Given
        var collectionId = new CollectionId("c1000");
        var labels = List.of(
                new LocalisedLabel("Collection Label FR", Lang.FR),
                new LocalisedLabel("Collection Label EN", Lang.EN)
        );
        var descriptions = List.of(
                new LocalisedLabel("Description FR", Lang.FR),
                new LocalisedLabel("Description EN", Lang.EN)
        );
        var created = LocalDateTime.of(2024, 1, 1, 10, 0);
        var modified = LocalDateTime.of(2024, 6, 1, 15, 30);
        var conceptIds = List.of(new ConceptId("concept1"), new ConceptId("concept2"));

        var collection = new Collection(
                collectionId,
                labels,
                "creator1",
                "contributor1",
                descriptions,
                created,
                modified,
                true,
                conceptIds
        );

        // When
        var graphDBCollection = GraphDBCollection.fromDomain(collection);

        // Then
        assertNotNull(graphDBCollection);
        assertEquals("c1000", graphDBCollection.id());
        assertEquals("Collection Label FR", graphDBCollection.prefLabelLg1());
        assertEquals("FR", graphDBCollection.prefLabelLg1_lg());
        assertEquals("Collection Label EN", graphDBCollection.prefLabelLg2());
        assertEquals("EN", graphDBCollection.prefLabelLg2_lg());
        assertEquals("Description FR", graphDBCollection.descriptionLg1());
        assertEquals("FR", graphDBCollection.descriptionLg1_lg());
        assertEquals("Description EN", graphDBCollection.descriptionLg2());
        assertEquals("EN", graphDBCollection.descriptionLg2_lg());
        assertEquals(created.toString(), graphDBCollection.created());
        assertEquals(modified.toString(), graphDBCollection.modified());
        assertTrue(graphDBCollection.isValidated());
        assertEquals("creator1", graphDBCollection.creator());
        assertEquals("contributor1", graphDBCollection.contributor());
        assertEquals(2, graphDBCollection.conceptIds().size());
        assertTrue(graphDBCollection.conceptIds().contains("concept1"));
        assertTrue(graphDBCollection.conceptIds().contains("concept2"));
    }

    @Test
    void should_convert_from_domain_with_null_optional_fields() {
        // Given
        var collectionId = new CollectionId("c2000");
        var labels = List.of(new LocalisedLabel("Collection Label", Lang.FR));
        var created = LocalDateTime.of(2024, 1, 1, 10, 0);

        var collection = new Collection(
                collectionId,
                labels,
                "creator1",
                null,
                List.of(),
                created,
                null,
                false,
                List.of()
        );

        // When
        var graphDBCollection = GraphDBCollection.fromDomain(collection);

        // Then
        assertNotNull(graphDBCollection);
        assertEquals("c2000", graphDBCollection.id());
        assertEquals("Collection Label", graphDBCollection.prefLabelLg1());
        assertEquals("FR", graphDBCollection.prefLabelLg1_lg());
        assertNull(graphDBCollection.prefLabelLg2());
        assertNull(graphDBCollection.prefLabelLg2_lg());
        assertNull(graphDBCollection.descriptionLg1());
        assertNull(graphDBCollection.descriptionLg1_lg());
        assertNull(graphDBCollection.descriptionLg2());
        assertNull(graphDBCollection.descriptionLg2_lg());
        assertNull(graphDBCollection.modified());
        assertNull(graphDBCollection.contributor());
        assertFalse(graphDBCollection.isValidated());
        assertTrue(graphDBCollection.conceptIds().isEmpty());
    }

    @Test
    void should_convert_from_domain_with_one_description() {
        // Given
        var collectionId = new CollectionId("c3000");
        var labels = List.of(new LocalisedLabel("Collection Label", Lang.FR));
        var descriptions = List.of(new LocalisedLabel("Description FR", Lang.FR));
        var created = LocalDateTime.of(2024, 1, 1, 10, 0);

        var collection = new Collection(
                collectionId,
                labels,
                "creator1",
                null,
                descriptions,
                created,
                null,
                false,
                List.of()
        );

        // When
        var graphDBCollection = GraphDBCollection.fromDomain(collection);

        // Then
        assertEquals("Description FR", graphDBCollection.descriptionLg1());
        assertEquals("FR", graphDBCollection.descriptionLg1_lg());
        assertNull(graphDBCollection.descriptionLg2());
        assertNull(graphDBCollection.descriptionLg2_lg());
    }

    @Test
    void should_convert_to_domain_with_all_fields() {
        // Given
        var graphDBCollection = new GraphDBCollection(
                "c1000",
                "Label FR",
                "fr",
                "Label EN",
                "en",
                "2024-01-01T10:00:00",
                "2024-06-01T15:30:00",
                "Description FR",
                "fr",
                "Description EN",
                "en",
                true,
                "creator1",
                "contributor1",
                List.of("concept1", "concept2")
        );

        // When
        var collection = graphDBCollection.toDomain();

        // Then
        assertNotNull(collection);
        assertEquals("c1000", collection.id().value());
        assertEquals("Label FR", collection.prefLabel().value());
        assertEquals(Lang.FR, collection.prefLabel().lang());
        assertEquals(1, collection.alternativeLabels().size());
        assertEquals("Label EN", collection.alternativeLabels().get(0).value());
        assertEquals(Lang.EN, collection.alternativeLabels().get(0).lang());
        assertEquals(2, collection.descriptions().size());
        assertEquals("Description FR", collection.descriptions().get(0).value());
        assertEquals(Lang.FR, collection.descriptions().get(0).lang());
        assertEquals("Description EN", collection.descriptions().get(1).value());
        assertEquals(Lang.EN, collection.descriptions().get(1).lang());
        assertEquals(LocalDateTime.parse("2024-01-01T10:00:00"), collection.created());
        assertEquals(LocalDateTime.parse("2024-06-01T15:30:00"), collection.modified().orElse(null));
        assertTrue(collection.isValidated());
        assertEquals("creator1", collection.creator());
        assertEquals("contributor1", collection.contributor().orElse(null));
        assertEquals(2, collection.conceptIds().size());
        assertEquals("concept1", collection.conceptIds().get(0).value());
        assertEquals("concept2", collection.conceptIds().get(1).value());
    }

    @Test
    void should_convert_to_domain_with_null_optional_fields() {
        // Given
        var graphDBCollection = new GraphDBCollection(
                "c2000",
                "Label FR",
                "fr",
                null,
                null,
                "2024-01-01T10:00:00",
                null,
                null,
                null,
                null,
                null,
                false,
                "creator1",
                null,
                List.of()
        );

        // When
        var collection = graphDBCollection.toDomain();

        // Then
        assertNotNull(collection);
        assertEquals("c2000", collection.id().value());
        assertEquals("Label FR", collection.prefLabel().value());
        assertTrue(collection.alternativeLabels().isEmpty());
        assertTrue(collection.descriptions().isEmpty());
        assertNull(collection.modified().orElse(null));
        assertNull(collection.contributor().orElse(null));
        assertFalse(collection.isValidated());
        assertTrue(collection.conceptIds().isEmpty());
    }

    @Test
    void should_convert_to_domain_with_only_first_label() {
        // Given
        var graphDBCollection = new GraphDBCollection(
                "c3000",
                "Label FR",
                "fr",
                null,
                null,
                "2024-01-01T10:00:00",
                null,
                "Description FR",
                "fr",
                null,
                null,
                false,
                "creator1",
                null,
                List.of()
        );

        // When
        var collection = graphDBCollection.toDomain();

        // Then
        assertEquals(1, collection.descriptions().size());
        assertEquals("Description FR", collection.descriptions().get(0).value());
        assertEquals(Lang.FR, collection.descriptions().get(0).lang());
    }

    @Test
    void should_replace_concepts_with_withConcepts_method() {
        // Given
        var originalGraphDBCollection = new GraphDBCollection(
                "c1000",
                "Label FR",
                "fr",
                "Label EN",
                "en",
                "2024-01-01T10:00:00",
                "2024-06-01T15:30:00",
                "Description FR",
                "fr",
                "Description EN",
                "en",
                true,
                "creator1",
                "contributor1",
                List.of("concept1", "concept2")
        );

        var newConcepts = new GraphDBConcept[]{
                new GraphDBConcept("concept3", "Concept 3 FR", "Concept 3 EN"),
                new GraphDBConcept("concept4", "Concept 4 FR", "Concept 4 EN"),
                new GraphDBConcept("concept5", "Concept 5 FR", "Concept 5 EN")
        };

        // When
        var updatedGraphDBCollection = originalGraphDBCollection.withConcepts(newConcepts);

        // Then
        assertNotNull(updatedGraphDBCollection);
        assertEquals(3, updatedGraphDBCollection.conceptIds().size());
        assertTrue(updatedGraphDBCollection.conceptIds().contains("concept3"));
        assertTrue(updatedGraphDBCollection.conceptIds().contains("concept4"));
        assertTrue(updatedGraphDBCollection.conceptIds().contains("concept5"));
        assertFalse(updatedGraphDBCollection.conceptIds().contains("concept1"));
        assertFalse(updatedGraphDBCollection.conceptIds().contains("concept2"));

        // Verify other fields remain unchanged
        assertEquals("c1000", updatedGraphDBCollection.id());
        assertEquals("Label FR", updatedGraphDBCollection.prefLabelLg1());
        assertEquals("fr", updatedGraphDBCollection.prefLabelLg1_lg());
        assertEquals("Label EN", updatedGraphDBCollection.prefLabelLg2());
        assertEquals("en", updatedGraphDBCollection.prefLabelLg2_lg());
        assertEquals("creator1", updatedGraphDBCollection.creator());
        assertEquals("contributor1", updatedGraphDBCollection.contributor());
        assertTrue(updatedGraphDBCollection.isValidated());
    }

    @Test
    void should_replace_concepts_with_empty_array() {
        // Given
        var originalGraphDBCollection = new GraphDBCollection(
                "c1000",
                "Label FR",
                "fr",
                null,
                null,
                "2024-01-01T10:00:00",
                null,
                null,
                null,
                null,
                null,
                false,
                "creator1",
                null,
                List.of("concept1", "concept2")
        );

        var newConcepts = new GraphDBConcept[]{};

        // When
        var updatedGraphDBCollection = originalGraphDBCollection.withConcepts(newConcepts);

        // Then
        assertTrue(updatedGraphDBCollection.conceptIds().isEmpty());
    }

    @Test
    void should_perform_round_trip_conversion() {
        // Given - Create a domain object
        var collectionId = new CollectionId("c1000");
        var labels = List.of(
                new LocalisedLabel("Collection Label FR", Lang.FR),
                new LocalisedLabel("Collection Label EN", Lang.EN)
        );
        var descriptions = List.of(
                new LocalisedLabel("Description FR", Lang.FR),
                new LocalisedLabel("Description EN", Lang.EN)
        );
        var created = LocalDateTime.of(2024, 1, 1, 10, 0);
        var modified = LocalDateTime.of(2024, 6, 1, 15, 30);
        var conceptIds = List.of(new ConceptId("concept1"), new ConceptId("concept2"));

        var originalCollection = new Collection(
                collectionId,
                labels,
                "creator1",
                "contributor1",
                descriptions,
                created,
                modified,
                true,
                conceptIds
        );

        // When - Convert to GraphDB and back to domain
        var graphDBCollection = GraphDBCollection.fromDomain(originalCollection);
        var convertedCollection = graphDBCollection.toDomain();

        // Then - Verify the round trip preserves all data
        assertEquals(originalCollection.id().value(), convertedCollection.id().value());
        assertEquals(originalCollection.prefLabel().value(), convertedCollection.prefLabel().value());
        assertEquals(originalCollection.prefLabel().lang(), convertedCollection.prefLabel().lang());
        assertEquals(originalCollection.alternativeLabels().size(), convertedCollection.alternativeLabels().size());
        assertEquals(originalCollection.descriptions().size(), convertedCollection.descriptions().size());
        assertEquals(originalCollection.created(), convertedCollection.created());
        assertEquals(originalCollection.modified().orElse(null), convertedCollection.modified().orElse(null));
        assertEquals(originalCollection.isValidated(), convertedCollection.isValidated());
        assertEquals(originalCollection.creator(), convertedCollection.creator());
        assertEquals(originalCollection.contributor().orElse(null), convertedCollection.contributor().orElse(null));
        assertEquals(originalCollection.conceptIds().size(), convertedCollection.conceptIds().size());
    }

    @Test
    void should_perform_round_trip_conversion_with_minimal_fields() {
        // Given
        var collectionId = new CollectionId("c2000");
        var labels = List.of(new LocalisedLabel("Collection Label", Lang.FR));
        var created = LocalDateTime.of(2024, 1, 1, 10, 0);

        var originalCollection = new Collection(
                collectionId,
                labels,
                "creator1",
                null,
                List.of(),
                created,
                null,
                false,
                List.of()
        );

        // When
        var graphDBCollection = GraphDBCollection.fromDomain(originalCollection);
        var convertedCollection = graphDBCollection.toDomain();

        // Then
        assertEquals(originalCollection.id().value(), convertedCollection.id().value());
        assertEquals(originalCollection.prefLabel().value(), convertedCollection.prefLabel().value());
        assertEquals(originalCollection.created(), convertedCollection.created());
        assertNull(convertedCollection.modified().orElse(null));
        assertNull(convertedCollection.contributor().orElse(null));
        assertFalse(convertedCollection.isValidated());
        assertTrue(convertedCollection.descriptions().isEmpty());
        assertTrue(convertedCollection.conceptIds().isEmpty());
    }

    @Test
    void should_parse_datetime_with_timezone_offset() {
        // Given - Date with timezone offset format
        // The parser extracts the local date-time part, ignoring the timezone offset
        var graphDBCollection = new GraphDBCollection(
                "c4000",
                "Label FR",
                "fr",
                null,
                null,
                "2010-02-17T00:00:00.000+01:00",
                "2024-06-01T15:30:00.000+02:00",
                null,
                null,
                null,
                null,
                false,
                "creator1",
                null,
                List.of()
        );

        // When
        var collection = graphDBCollection.toDomain();

        // Then
        assertNotNull(collection);
        assertEquals(LocalDateTime.of(2010, 2, 17, 0, 0, 0), collection.created());
        assertEquals(LocalDateTime.of(2024, 6, 1, 15, 30, 0), collection.modified().orElse(null));
    }

    @Test
    void should_parse_datetime_without_timezone_offset() {
        // Given - Date without timezone offset (standard LocalDateTime format)
        var graphDBCollection = new GraphDBCollection(
                "c5000",
                "Label FR",
                "fr",
                null,
                null,
                "2024-01-01T10:00:00",
                "2024-06-01T15:30:00",
                null,
                null,
                null,
                null,
                false,
                "creator1",
                null,
                List.of()
        );

        // When
        var collection = graphDBCollection.toDomain();

        // Then
        assertNotNull(collection);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0, 0), collection.created());
        assertEquals(LocalDateTime.of(2024, 6, 1, 15, 30, 0), collection.modified().orElse(null));
    }
}