package fr.insee.rmes.modules.operations.documents.infrastructure.graphdb;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentLanguage;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Documents et liens en gestion contre un vrai GraphDB. Chaque test recharge le jeu de données : ceux
 * qui écrivent (enregistrement, suppression) partent ainsi de la même base.
 */
@Tag("integration")
class GraphDBManagedDocumentRepositoryIntegrationTest extends WithGraphDBContainer {

    private static final String DOCUMENT_12 = "http://bauhaus/documents/document/12";
    private static final String LINK_13 = "http://bauhaus/documents/page/13";
    private static final String DOCUMENT_112 = "http://bauhaus/documents/document/112";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final GraphDBManagedDocumentRepository repository = new GraphDBManagedDocumentRepository(
            repositoryGestion,
            GraphsPropertiesStub.stub(),
            new BauhausUriProperties("http://bauhaus/", "documents/page", "codes", "documents/document", "produits"),
            new BauhausLanguagesProperties("fr", "en"));

    @BeforeEach
    void loadData() throws Exception {
        repositoryGestion.executeUpdate("CLEAR ALL");
        fixtureLoader().withTrigFiles("managed-documents-it.trig");
    }

    @Test
    void should_read_every_field_of_a_document() throws Exception {
        assertThat(repository.find(DocumentKind.DOCUMENT, "12"))
                .contains(new ManagedDocument(
                        "12",
                        DocumentKind.DOCUMENT,
                        DOCUMENT_12,
                        new DocumentForm(
                                "Note technique",
                                "Technical note",
                                "Une note",
                                "A note",
                                "2026-04-09",
                                "fr",
                                "file:///storage/gestion/Note_technique.pdf"),
                        new FileSize(130_048)));
    }

    @Test
    void should_read_a_link_and_only_as_a_link() throws Exception {
        assertThat(repository.find(DocumentKind.LINK, "13")).hasValueSatisfying(link -> {
            assertThat(link.uri()).isEqualTo(LINK_13);
            assertThat(link.form().url()).isEqualTo("https://www.insee.fr/fr/statistiques/13");
            assertThat(link.size()).isNull();
        });
        assertThat(repository.find(DocumentKind.DOCUMENT, "13")).isEmpty();
    }

    @Test
    void should_list_documents_and_links() throws Exception {
        assertThat(repository.findAll())
                .extracting(ManagedDocument::uri, ManagedDocument::kind)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(DOCUMENT_12, DocumentKind.DOCUMENT),
                        org.assertj.core.groups.Tuple.tuple(LINK_13, DocumentKind.LINK),
                        org.assertj.core.groups.Tuple.tuple(DOCUMENT_112, DocumentKind.DOCUMENT));
    }

    @Test
    void should_build_the_iri_of_a_document_and_of_a_link() {
        assertThat(repository.uriOf(DocumentKind.DOCUMENT, "5")).isEqualTo("http://bauhaus/documents/document/5");
        assertThat(repository.uriOf(DocumentKind.LINK, "5")).isEqualTo("http://bauhaus/documents/page/5");
    }

    @Test
    void should_continue_the_identifier_sequence_shared_by_documents_and_links() throws Exception {
        assertThat(repository.nextId()).isEqualTo("113");
    }

    @Test
    void should_write_a_new_document_with_its_size() throws Exception {
        ManagedDocument created = new ManagedDocument(
                "5000",
                DocumentKind.DOCUMENT,
                "http://bauhaus/documents/document/5000",
                new DocumentForm(
                        "Nouveau", null, null, null, "2026-09-24", "fr", "file:///storage/gestion/Nouveau.pdf"),
                new FileSize(42));

        repository.save(created);

        assertThat(repository.find(DocumentKind.DOCUMENT, "5000")).contains(created);
    }

    @Test
    void should_replace_everything_stored_for_the_document_when_saving_it() throws Exception {
        ManagedDocument stored = repository.find(DocumentKind.DOCUMENT, "12").orElseThrow();
        DocumentForm withoutDescriptions = new DocumentForm(
                "Note révisée", null, null, null, null, null, stored.form().url());

        repository.save(stored.withForm(withoutDescriptions));

        assertThat(repository.find(DocumentKind.DOCUMENT, "12")).contains(stored.withForm(withoutDescriptions));
    }

    @Test
    void should_tell_whether_another_document_or_link_carries_a_label() throws Exception {
        assertThat(repository.isLabelUsedByAnother("Note technique", DocumentLanguage.FIRST, LINK_13))
                .isTrue();
        assertThat(repository.isLabelUsedByAnother("Note technique", DocumentLanguage.FIRST, DOCUMENT_12))
                .isFalse();
        assertThat(repository.isLabelUsedByAnother("Technical note", DocumentLanguage.SECOND, LINK_13))
                .isTrue();
        assertThat(repository.isLabelUsedByAnother("Technical note", DocumentLanguage.FIRST, LINK_13))
                .isFalse();
    }

    @Test
    void should_find_the_owner_of_an_url_whatever_its_case() throws Exception {
        assertThat(repository.findUriByUrl("https://www.INSEE.fr/fr/statistiques/13"))
                .contains(LINK_13);
        assertThat(repository.findUriByUrl("https://www.insee.fr/ailleurs")).isEmpty();
    }

    @Test
    void should_find_the_texts_citing_exactly_this_document() throws Exception {
        assertThat(repository.findSimsTextsCiting(DOCUMENT_112))
                .containsExactly("http://bauhaus/qualite/attribut/9902/S.3.1/texte");
        assertThat(repository.findSimsTextsCiting(DOCUMENT_12)).isEmpty();
    }

    @Test
    void should_give_the_quality_reports_citing_a_document() throws Exception {
        assertThat(repository.findSimsReferences(DocumentKind.DOCUMENT, "112"))
                .containsExactly(new SimsReference("9902", "Rapport citant", "Citing report", "S.3.1", List.of()));
    }

    @Test
    void should_delete_everything_stored_for_a_document() throws Exception {
        repository.delete(DOCUMENT_12);

        assertThat(repository.find(DocumentKind.DOCUMENT, "12")).isEmpty();
        assertThat(repositoryGestion.getResponseAsBoolean("ASK { GRAPH ?g { <" + DOCUMENT_12 + "> ?p ?o } }"))
                .isFalse();
    }
}
