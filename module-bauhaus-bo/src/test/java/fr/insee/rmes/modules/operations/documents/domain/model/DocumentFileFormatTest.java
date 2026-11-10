package fr.insee.rmes.modules.operations.documents.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DocumentFileFormatTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "archive.7z, application/x-7z-compressed",
        "donnees.csv, text/csv",
        "note.doc, application/msword",
        "note.docx, application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "page.html, text/html",
        "tableau.ods, application/vnd.oasis.opendocument.spreadsheet",
        "note.odt, application/vnd.oasis.opendocument.text",
        "Note_technique.pdf, application/pdf",
        "tableau.xls, application/vnd.ms-excel",
        "tableau.xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "archive.zip, application/zip"
    })
    void should_give_the_media_type_matching_the_extension(String fileName, String mediaType) {
        assertThat(DocumentFileFormat.mediaTypeOf(fileName)).isEqualTo(mediaType);
    }

    @Test
    void should_ignore_the_case_of_the_extension() {
        assertThat(DocumentFileFormat.mediaTypeOf("NOTE.PDF")).isEqualTo("application/pdf");
    }

    @Test
    void should_fall_back_to_a_binary_stream_for_an_unknown_extension() {
        assertThat(DocumentFileFormat.mediaTypeOf("image.png")).isEqualTo("application/octet-stream");
    }

    @Test
    void should_fall_back_to_a_binary_stream_without_extension() {
        assertThat(DocumentFileFormat.mediaTypeOf("fichier")).isEqualTo("application/octet-stream");
    }
}
