package fr.insee.rmes.modules.operations.documents.domain;

import static fr.insee.rmes.modules.operations.documents.domain.InMemoryDocumentFileStorage.URL_PREFIX;
import static fr.insee.rmes.modules.operations.documents.domain.InMemoryManagedDocumentRepository.form;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException.Violation;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.StoredFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDetails;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import fr.insee.rmes.modules.operations.documents.domain.model.StoredFile;
import fr.insee.rmes.modules.operations.documents.domain.model.UploadedFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DomainDocumentManagementServiceTest {

    private final InMemoryManagedDocumentRepository repository = new InMemoryManagedDocumentRepository();
    private final InMemoryDocumentFileStorage storage = new InMemoryDocumentFileStorage();
    private final InMemorySimsOwnersLookup owners = new InMemorySimsOwnersLookup();
    private final DomainDocumentManagementService service =
            new DomainDocumentManagementService(repository, storage, owners, Set.of("pdf", "odt"));

    private static UploadedFile upload(String name, String content) {
        return new UploadedFile(name, new ByteArrayInputStream(content.getBytes()), content.length());
    }

    private ManagedDocument existingDocument(String id, String labelLg1, String fileName, FileSize size) {
        storage.put(fileName, "existant");
        ManagedDocument document = new ManagedDocument(
                id,
                DocumentKind.DOCUMENT,
                repository.uriOf(DocumentKind.DOCUMENT, id),
                form(labelLg1, null, URL_PREFIX + fileName),
                size);
        repository.add(document);
        return document;
    }

    private ManagedDocument existingLink(String id, String labelLg1, String url) {
        ManagedDocument link = new ManagedDocument(
                id, DocumentKind.LINK, repository.uriOf(DocumentKind.LINK, id), form(labelLg1, null, url), null);
        repository.add(link);
        return link;
    }

    private static void assertViolation(Executable call, Violation violation) {
        assertThatThrownBy(call::execute)
                .isInstanceOfSatisfying(
                        DocumentRuleViolationException.class,
                        e -> assertThat(e.violation()).isEqualTo(violation));
    }

    @Nested
    class CreatingADocument {

        @Test
        void should_store_the_file_and_record_its_url_and_its_size() throws Exception {
            String id = service.createDocument(form("Note", "Note EN", null), upload("Note_technique.pdf", "12345"));

            assertThat(storage.content("Note_technique.pdf")).isEqualTo("12345");
            assertThat(repository.stored(DocumentKind.DOCUMENT, id)).hasValueSatisfying(document -> {
                assertThat(document.form().url()).isEqualTo(URL_PREFIX + "Note_technique.pdf");
                assertThat(document.size()).isEqualTo(new FileSize(5));
                assertThat(document.form().labelLg1()).isEqualTo("Note");
            });
        }

        @Test
        void should_take_its_identifier_from_the_sequence_shared_with_links() throws Exception {
            assertThat(service.createDocument(form("A", null, null), upload("a.pdf", "x")))
                    .isEqualTo("1000");
            assertThat(service.createLink(form("B", null, "https://www.insee.fr/b")))
                    .isEqualTo("1001");
        }

        @Test
        void should_refuse_a_file_without_name() {
            assertViolation(
                    () -> service.createDocument(form("Note", null, null), upload("", "x")), Violation.FILE_EMPTY_NAME);
        }

        @ParameterizedTest
        @ValueSource(strings = {"note technique.pdf", "note/technique.pdf", "notetechnique", "note.é"})
        void should_refuse_a_file_name_with_forbidden_characters(String fileName) {
            assertViolation(
                    () -> service.createDocument(form("Note", null, null), upload(fileName, "x")),
                    Violation.FILE_FORBIDDEN_CHARACTERS);
        }

        @Test
        void should_refuse_a_file_name_already_used_in_the_storage() {
            existingDocument("12", "Autre", "Note.pdf", null);

            assertViolation(
                    () -> service.createDocument(form("Note", null, null), upload("Note.pdf", "x")),
                    Violation.FILE_ALREADY_EXISTS);
            assertThat(storage.content("Note.pdf")).isEqualTo("existant");
        }

        @Test
        void should_refuse_a_label_already_used_by_another_document_or_link() {
            existingLink("12", "Note", "https://www.insee.fr/note");

            assertViolation(
                    () -> service.createDocument(form("Note", null, null), upload("Note.pdf", "x")),
                    Violation.LABEL_LG1_ALREADY_USED);
            assertThat(storage.exists("Note.pdf")).isFalse();
        }
    }

    @Nested
    class CreatingALink {

        @Test
        void should_store_the_link_with_its_url_and_no_size() throws Exception {
            String id = service.createLink(form("Page", "Page EN", "https://www.insee.fr/page"));

            assertThat(repository.stored(DocumentKind.LINK, id)).hasValueSatisfying(link -> {
                assertThat(link.form().url()).isEqualTo("https://www.insee.fr/page");
                assertThat(link.size()).isNull();
            });
        }

        @Test
        void should_refuse_an_empty_url() {
            assertViolation(() -> service.createLink(form("Page", null, "")), Violation.LINK_EMPTY_URL);
        }

        @Test
        void should_refuse_an_invalid_url() {
            assertViolation(() -> service.createLink(form("Page", null, "pas une url")), Violation.LINK_BAD_URL);
        }

        @Test
        void should_refuse_an_url_already_used_whatever_its_case() {
            existingLink("12", "Autre", "https://www.insee.fr/page");

            assertViolation(
                    () -> service.createLink(form("Page", null, "https://www.INSEE.fr/page")),
                    Violation.LINK_URL_ALREADY_USED);
        }

        @Test
        void should_refuse_a_second_language_label_already_used() {
            ManagedDocument other = existingLink("12", "Autre", "https://www.insee.fr/autre");
            repository.save(other.withForm(form("Autre", "Page EN", "https://www.insee.fr/autre")));

            assertViolation(
                    () -> service.createLink(form("Page", "Page EN", "https://www.insee.fr/page")),
                    Violation.LABEL_LG2_ALREADY_USED);
        }
    }

    @Nested
    class UpdatingADocument {

        @Test
        void should_keep_the_file_url_and_the_size_whatever_the_form_says() throws Exception {
            existingDocument("12", "Note", "Note.pdf", new FileSize(130_048));

            service.update(DocumentKind.DOCUMENT, "12", form("Note révisée", "Note EN", null));

            assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).hasValueSatisfying(document -> {
                assertThat(document.form().labelLg1()).isEqualTo("Note révisée");
                assertThat(document.form().url()).isEqualTo(URL_PREFIX + "Note.pdf");
                assertThat(document.size()).isEqualTo(new FileSize(130_048));
            });
        }

        @Test
        void should_accept_the_labels_it_already_carries() throws Exception {
            existingDocument("12", "Note", "Note.pdf", null);

            service.update(DocumentKind.DOCUMENT, "12", form("Note", null, null));

            assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).isPresent();
        }

        @Test
        void should_take_the_url_of_a_link_from_the_form() throws Exception {
            existingLink("12", "Page", "https://www.insee.fr/avant");

            service.update(DocumentKind.LINK, "12", form("Page", null, "https://www.insee.fr/apres"));

            assertThat(repository.stored(DocumentKind.LINK, "12"))
                    .hasValueSatisfying(link -> assertThat(link.form().url()).isEqualTo("https://www.insee.fr/apres"));
        }

        @Test
        void should_fail_for_an_unknown_document() {
            assertThatThrownBy(() -> service.update(DocumentKind.DOCUMENT, "404", form("Note", null, null)))
                    .isInstanceOf(DocumentNotFoundException.class);
        }
    }

    @Nested
    class ReplacingTheFile {

        @Test
        void should_overwrite_a_file_of_the_same_name_keep_the_url_and_update_the_size() throws Exception {
            existingDocument("12", "Note", "Note.pdf", new FileSize(8));

            assertThat(service.replaceFile("12", upload("Note.pdf", "nouveau contenu")))
                    .isEmpty();

            assertThat(storage.content("Note.pdf")).isEqualTo("nouveau contenu");
            assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).hasValueSatisfying(document -> {
                assertThat(document.form().url()).isEqualTo(URL_PREFIX + "Note.pdf");
                assertThat(document.size()).isEqualTo(new FileSize(15));
            });
        }

        @Test
        void should_move_to_a_file_of_another_name_and_give_its_url() throws Exception {
            existingDocument("12", "Note", "Note.pdf", new FileSize(8));

            assertThat(service.replaceFile("12", upload("Note_v2.pdf", "v2"))).contains(URL_PREFIX + "Note_v2.pdf");

            assertThat(storage.exists("Note.pdf")).isFalse();
            assertThat(storage.content("Note_v2.pdf")).isEqualTo("v2");
            assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).hasValueSatisfying(document -> {
                assertThat(document.form().url()).isEqualTo(URL_PREFIX + "Note_v2.pdf");
                assertThat(document.size()).isEqualTo(new FileSize(2));
            });
        }

        @Test
        void should_refuse_a_new_name_already_used_by_another_file() {
            existingDocument("12", "Note", "Note.pdf", null);
            existingDocument("13", "Autre", "Autre.pdf", null);

            assertViolation(() -> service.replaceFile("12", upload("Autre.pdf", "x")), Violation.FILE_ALREADY_EXISTS);
            assertThat(storage.content("Autre.pdf")).isEqualTo("existant");
        }

        @ParameterizedTest
        @ValueSource(strings = {"charge.exe", "sansextension"})
        void should_refuse_an_extension_that_is_not_allowed(String fileName) {
            existingDocument("12", "Note", "Note.pdf", null);

            assertViolation(
                    () -> service.replaceFile("12", upload(fileName, "x")), Violation.FILE_EXTENSION_NOT_ALLOWED);
        }

        @Test
        void should_fail_for_a_link() {
            existingLink("12", "Page", "https://www.insee.fr/page");

            assertThatThrownBy(() -> service.replaceFile("12", upload("Note.pdf", "x")))
                    .isInstanceOf(DocumentNotFoundException.class);
        }
    }

    @Nested
    class Deleting {

        @Test
        void should_delete_a_document_and_its_file() throws Exception {
            existingDocument("12", "Note", "Note.pdf", null);

            service.delete(DocumentKind.DOCUMENT, "12");

            assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).isEmpty();
            assertThat(storage.exists("Note.pdf")).isFalse();
        }

        @Test
        void should_delete_a_link() throws Exception {
            existingLink("12", "Page", "https://www.insee.fr/page");

            service.delete(DocumentKind.LINK, "12");

            assertThat(repository.stored(DocumentKind.LINK, "12")).isEmpty();
        }

        @Test
        void should_refuse_to_delete_a_document_cited_by_a_quality_report() {
            ManagedDocument document = existingDocument("12", "Note", "Note.pdf", null);
            repository.citedBy(document.uri(), "http://bauhaus/qualite/attribut/1/S.3.1/texte");

            assertViolation(() -> service.delete(DocumentKind.DOCUMENT, "12"), Violation.REFERENCED_BY_SIMS);
            assertThat(repository.stored(DocumentKind.DOCUMENT, "12")).isPresent();
            assertThat(storage.exists("Note.pdf")).isTrue();
        }

        @Test
        void should_fail_for_an_unknown_link() {
            assertThatThrownBy(() -> service.delete(DocumentKind.LINK, "404"))
                    .isInstanceOf(DocumentNotFoundException.class);
        }
    }

    @Nested
    class Reading {

        @Test
        void should_give_a_document_with_the_quality_reports_citing_it_and_their_owners() throws Exception {
            ManagedDocument document = existingDocument("12", "Note", "Note.pdf", null);
            repository.referencedBy(document.uri(), new SimsReference("1", "Rapport", "Report", "S.3.1", List.of()));
            owners.owners("1", List.of("DG75-L201"));

            DocumentDetails details = service.get(DocumentKind.DOCUMENT, "12");

            assertThat(details.document()).isEqualTo(document);
            assertThat(details.sims())
                    .containsExactly(new SimsReference("1", "Rapport", "Report", "S.3.1", List.of("DG75-L201")));
        }

        @Test
        void should_list_documents_and_links() throws Exception {
            existingDocument("12", "Note", "Note.pdf", null);
            existingLink("13", "Page", "https://www.insee.fr/page");

            assertThat(service.getAll()).extracting(ManagedDocument::id).containsExactly("12", "13");
        }

        @Test
        void should_download_the_file_of_a_document() throws Exception {
            existingDocument("12", "Note", "Note.pdf", null);

            StoredFile file = service.download("12");

            assertThat(file.name()).isEqualTo("Note.pdf");
            try (InputStream content = file.content()) {
                assertThat(content).hasContent("existant");
            }
        }

        @Test
        void should_fail_to_download_a_file_missing_from_the_storage() {
            existingDocument("12", "Note", "Note.pdf", null);
            storage.delete("Note.pdf");

            assertThatThrownBy(() -> service.download("12")).isInstanceOf(StoredFileNotFoundException.class);
        }

        @Test
        void should_fail_to_read_an_unknown_document() {
            assertThatThrownBy(() -> service.get(DocumentKind.DOCUMENT, "404"))
                    .isInstanceOf(DocumentNotFoundException.class);
        }
    }
}
