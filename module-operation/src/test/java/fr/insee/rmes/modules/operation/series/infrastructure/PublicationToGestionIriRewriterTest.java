package fr.insee.rmes.modules.operation.series.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PublicationToGestionIriRewriterTest {

    private final PublicationToGestionIriRewriter rewriter =
            new PublicationToGestionIriRewriter("http://id.insee.fr/", "http://bauhaus/");

    @Test
    void rewritesPublicationPrefixToGestionPrefix() {
        assertThat(rewriter.toGestion("http://id.insee.fr/operations/serie/s1001"))
                .isEqualTo("http://bauhaus/operations/serie/s1001");
    }

    @Test
    void leavesGestionIriUnchanged() {
        assertThat(rewriter.toGestion("http://bauhaus/operations/serie/s1001"))
                .isEqualTo("http://bauhaus/operations/serie/s1001");
    }

    @Test
    void leavesIriWithoutKnownPrefixUnchanged() {
        assertThat(rewriter.toGestion("http://other.example/operations/serie/s1001"))
                .isEqualTo("http://other.example/operations/serie/s1001");
    }

    @Test
    void returnsNullWhenInputIsNull() {
        assertThat(rewriter.toGestion(null)).isNull();
    }

    @Test
    void returnsBlankUnchanged() {
        assertThat(rewriter.toGestion("   ")).isEqualTo("   ");
    }
}
