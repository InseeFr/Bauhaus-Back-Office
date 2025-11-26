package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import org.eclipse.rdf4j.model.IRI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphDbCompactOrganisationTest {

    @Test
    void shouldConvertToDomainWithFrenchLabel() {
        // Given
        String iriString = "http://rdf.insee.fr/def/base#OrganismUnit_1234";
        String identifier = "ORG-001";
        String label = "Direction des statistiques";
        String labelLg = "fr";

        GraphDbCompactOrganisation graphDbOrganisation = new GraphDbCompactOrganisation(
            iriString,
            identifier,
            label,
            labelLg
        );

        // When
        CompactOrganisation result = graphDbOrganisation.toDomain();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.iri()).isInstanceOf(IRI.class);
        assertThat(result.iri().stringValue()).isEqualTo(iriString);
        assertThat(result.identifier()).isEqualTo(identifier);
        assertThat(result.label().value()).isEqualTo(label);
        assertThat(result.label().lang()).isEqualTo(Lang.FR);
    }

    @Test
    void shouldConvertToDomainWithEnglishLabel() {
        // Given
        String iriString = "http://rdf.insee.fr/def/base#OrganismUnit_5678";
        String identifier = "ORG-002";
        String label = "Statistics Department";
        String labelLg = "en";

        GraphDbCompactOrganisation graphDbOrganisation = new GraphDbCompactOrganisation(
            iriString,
            identifier,
            label,
            labelLg
        );

        // When
        CompactOrganisation result = graphDbOrganisation.toDomain();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.iri()).isInstanceOf(IRI.class);
        assertThat(result.iri().stringValue()).isEqualTo(iriString);
        assertThat(result.identifier()).isEqualTo(identifier);
        assertThat(result.label().value()).isEqualTo(label);
        assertThat(result.label().lang()).isEqualTo(Lang.EN);
    }

    @Test
    void shouldHandleLabelLanguageInUpperCase() {
        // Given
        String iriString = "http://rdf.insee.fr/def/base#OrganismUnit_9999";
        String identifier = "ORG-003";
        String label = "Test Organization";
        String labelLg = "FR"; // Already uppercase

        GraphDbCompactOrganisation graphDbOrganisation = new GraphDbCompactOrganisation(
            iriString,
            identifier,
            label,
            labelLg
        );

        // When
        CompactOrganisation result = graphDbOrganisation.toDomain();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.label().lang()).isEqualTo(Lang.FR);
    }

    @Test
    void shouldCreateValidIRIFromString() {
        // Given
        String iriString = "http://example.org/org#123";
        GraphDbCompactOrganisation graphDbOrganisation = new GraphDbCompactOrganisation(
            iriString,
            "ID-123",
            "Label",
            "fr"
        );

        // When
        CompactOrganisation result = graphDbOrganisation.toDomain();

        // Then
        IRI iri = result.iri();
        assertThat(iri.stringValue()).isEqualTo(iriString);
        assertThat(iri.getNamespace()).isEqualTo("http://example.org/org#");
        assertThat(iri.getLocalName()).isEqualTo("123");
    }
}
