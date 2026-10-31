package fr.insee.rmes.modules.operations.documents.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.modules.commons.infrastructure.filessystem.FileSystemOperation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Le stockage des fichiers de documents sur le vrai adaptateur disque. */
class FilesOperationsDocumentFileStorageTest {

    @TempDir
    Path root;

    private Path gestion;
    private FilesOperationsDocumentFileStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        gestion = Files.createDirectories(root.resolve("gestion"));
        storage = new FilesOperationsDocumentFileStorage(
                new FileSystemOperation(), gestion.toString(), gestion.toString());
    }

    @Test
    void should_write_the_file_and_give_its_url() {
        String url = storage.write("Note.pdf", new ByteArrayInputStream("contenu".getBytes()));

        assertThat(url).isEqualTo("file://" + gestion.resolve("Note.pdf"));
        assertThat(gestion.resolve("Note.pdf")).hasContent("contenu");
        assertThat(storage.exists("Note.pdf")).isTrue();
    }

    @Test
    void should_read_a_stored_file() throws Exception {
        Files.writeString(gestion.resolve("Note.pdf"), "contenu");

        try (InputStream content = storage.read("Note.pdf")) {
            assertThat(content).hasContent("contenu");
        }
    }

    @Test
    void should_delete_a_stored_file() throws Exception {
        Files.writeString(gestion.resolve("Note.pdf"), "contenu");

        storage.delete("Note.pdf");

        assertThat(storage.exists("Note.pdf")).isFalse();
    }

    @Test
    void should_know_that_a_file_is_absent() {
        assertThat(storage.exists("Absent.pdf")).isFalse();
    }

    @Test
    void should_give_the_file_name_of_an_url() {
        assertThat(storage.fileNameOf("file:///opt/app/data/storage/gestion/Note_technique.pdf"))
                .isEqualTo("Note_technique.pdf");
    }
}
