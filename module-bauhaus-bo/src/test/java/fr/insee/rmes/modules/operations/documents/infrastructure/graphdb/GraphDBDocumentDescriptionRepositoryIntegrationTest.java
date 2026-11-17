package fr.insee.rmes.modules.operations.documents.infrastructure.graphdb;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class GraphDBDocumentDescriptionRepositoryIntegrationTest extends WithGraphDBContainer {

    private static final String DOC_METHOD = "http://ec.europa.eu/eurostat/simsv2/concept/DOC_METHOD";
    private static final String COLLECTION_DOCUMENTS = "http://bauhaus/concepts/simsv2fr/COLLECTION_DOCUMENTS";

    private static GraphDBDocumentDescriptionRepository repository;

    @BeforeAll
    static void initData() {
        fixtureLoader().withTrigFiles("document-description-it.trig");

        RepositoryGestion repositoryGestion = new RepositoryGestion(
                getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

        repository = new GraphDBDocumentDescriptionRepository(
                repositoryGestion,
                GraphsPropertiesStub.stub(),
                new BauhausUriProperties(
                        "http://bauhaus/", "documents/page", "codes", "documents/document", "produits/indicateur"),
                new BauhausLanguagesProperties("fr", "en"));
    }

    @Test
    void should_read_every_field_of_a_document() throws RmesException {
        assertThat(repository.findMetadata(DocumentKind.DOCUMENT, "9001"))
                .contains(new DocumentMetadata(
                        "http://bauhaus/documents/document/9001",
                        List.of(
                                LocalisedLabel.ofDefaultLanguage("Les projections de population 2026"),
                                LocalisedLabel.ofAlternativeLanguage("Population projections for 2026")),
                        List.of(
                                LocalisedLabel.ofDefaultLanguage("Commentaire sur les projections"),
                                LocalisedLabel.ofAlternativeLanguage("Comment on population projections")),
                        LocalDate.of(2026, 4, 9),
                        "fr",
                        new FileSize(130_048),
                        "https://www.insee.fr/fr/metadonnees/source/fichier/Note_technique.pdf"));
    }

    @Test
    void should_read_a_link_and_not_the_document_sharing_its_identifier() throws RmesException {
        assertThat(repository.findMetadata(DocumentKind.LINK, "9001")).hasValueSatisfying(link -> {
            assertThat(link.uri()).isEqualTo("http://bauhaus/documents/page/9001");
            assertThat(link.url()).isEqualTo("https://www.insee.fr/fr/statistiques/9001");
        });
    }

    @Test
    void should_leave_the_optional_fields_empty_when_they_are_not_stored() throws RmesException {
        assertThat(repository.findMetadata(DocumentKind.DOCUMENT, "9002"))
                .contains(new DocumentMetadata(
                        "http://bauhaus/documents/document/9002",
                        List.of(LocalisedLabel.ofDefaultLanguage("Document minimal")),
                        List.of(),
                        null,
                        null,
                        null,
                        "https://www.insee.fr/fr/metadonnees/source/fichier/minimal.pdf"));
    }

    @Test
    void should_ignore_a_size_that_is_not_a_number_of_bytes() throws RmesException {
        assertThat(repository.findMetadata(DocumentKind.DOCUMENT, "9003"))
                .hasValueSatisfying(document -> assertThat(document.size()).isNull());
    }

    @Test
    void should_find_nothing_for_an_unknown_identifier() throws RmesException {
        assertThat(repository.findMetadata(DocumentKind.DOCUMENT, "404404")).isEmpty();
    }

    @Test
    void should_find_nothing_for_an_identifier_that_is_not_an_iri_segment() throws RmesException {
        assertThat(repository.findMetadata(DocumentKind.DOCUMENT, "9001>")).isEmpty();
    }

    @Test
    void should_read_the_concept_of_the_rubric_citing_the_document() throws RmesException {
        assertThat(repository.findRubricConcepts("http://bauhaus/documents/document/9001"))
                .containsExactly(DOC_METHOD);
    }

    @Test
    void should_read_the_concepts_of_every_rubric_citing_the_document() throws RmesException {
        assertThat(repository.findRubricConcepts("http://bauhaus/documents/document/9003"))
                .containsExactlyInAnyOrder(DOC_METHOD, COLLECTION_DOCUMENTS);
    }

    @Test
    void should_read_no_concept_for_a_document_cited_nowhere() throws RmesException {
        assertThat(repository.findRubricConcepts("http://bauhaus/documents/document/9002"))
                .isEmpty();
    }
}
