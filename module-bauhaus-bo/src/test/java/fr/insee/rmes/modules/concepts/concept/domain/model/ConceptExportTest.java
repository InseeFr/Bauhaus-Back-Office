package fr.insee.rmes.modules.concepts.concept.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConceptExportTest {

    @Test
    void exposes_file_name_content_and_content_type() {
        byte[] payload = new byte[]{'P', 'K', 0x03, 0x04};
        var export = new ConceptExport("c00001-mon-concept.odt", payload, "application/vnd.oasis.opendocument.text");

        assertThat(export.fileName()).isEqualTo("c00001-mon-concept.odt");
        assertThat(export.content()).isSameAs(payload);
        assertThat(export.contentType()).isEqualTo("application/vnd.oasis.opendocument.text");
    }
}
