package fr.insee.rmes.modules.operations.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.insee.rmes.modules.commons.infrastructure.filessystem.FileSystemOperation;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.PublishedFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.PublishedFile;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DomainPublishedDocumentFileServiceTest {

    @TempDir
    Path storage;

    private DomainPublishedDocumentFileService service;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(storage.resolve("publication"));
        Files.createDirectories(storage.resolve("gestion"));
        Files.writeString(storage.resolve("publication/Note_technique.pdf"), "contenu publié");
        Files.writeString(storage.resolve("gestion/Brouillon.pdf"), "contenu en gestion");

        service = new DomainPublishedDocumentFileService(
                new FileSystemOperation(), storage.resolve("publication").toString());
    }

    @Test
    void should_serve_a_published_file_with_the_media_type_of_its_extension() throws Exception {
        PublishedFile file = service.getPublishedFile("Note_technique.pdf");

        assertThat(file.name()).isEqualTo("Note_technique.pdf");
        assertThat(file.mediaType()).isEqualTo("application/pdf");
        try (InputStream content = file.content()) {
            assertThat(content).hasContent("contenu publié");
        }
    }

    @Test
    void should_not_serve_a_file_that_is_not_published() {
        assertThatThrownBy(() -> service.getPublishedFile("Brouillon.pdf"))
                .isInstanceOf(PublishedFileNotFoundException.class)
                .hasMessage("File Brouillon.pdf is not published");
    }

    @ParameterizedTest
    @ValueSource(strings = {"../gestion/Brouillon.pdf", "..\\gestion\\Brouillon.pdf", "..", ".", ""})
    void should_not_serve_anything_outside_the_publication_directory(String fileName) {
        assertThatThrownBy(() -> service.getPublishedFile(fileName)).isInstanceOf(PublishedFileNotFoundException.class);
    }
}
