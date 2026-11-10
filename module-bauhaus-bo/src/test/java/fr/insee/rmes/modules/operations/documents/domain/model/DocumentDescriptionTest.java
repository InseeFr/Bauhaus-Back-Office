package fr.insee.rmes.modules.operations.documents.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

class DocumentDescriptionTest {

    @Test
    void should_expose_the_stored_size() {
        assertThat(description(new FileSize(130_048)).size()).isEqualTo(new FileSize(130_048));
    }

    @Test
    void should_expose_a_zero_size_when_none_is_stored() {
        assertThat(description(null).size()).isEqualTo(new FileSize(0));
    }

    private static DocumentDescription description(@Nullable FileSize size) {
        return new DocumentDescription(
                new DocumentMetadata(
                        "http://bauhaus/documents/document/1", List.of(), List.of(), null, null, size, "https://x"),
                DocumentType.OTHER);
    }
}
