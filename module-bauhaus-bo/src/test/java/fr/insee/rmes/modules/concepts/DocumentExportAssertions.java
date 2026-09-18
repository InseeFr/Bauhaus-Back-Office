package fr.insee.rmes.modules.concepts;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Assertions partagées par les tests end-to-end des exports de concepts et de collections. */
public final class DocumentExportAssertions {

    private DocumentExportAssertions() {}

    /**
     * La réponse est un document OpenDocument (archive zip, signature « PK ») servi en pièce jointe, dont
     * l'en-tête Content-Disposition commence par {@code expectedContentDispositionPrefix}.
     */
    public static void assertDownloadableOpenDocument(
            ResponseEntity<byte[]> exportResponse, String expectedContentDispositionPrefix) {
        assertThat(exportResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        byte[] body = exportResponse.getBody();
        assertThat(body).isNotNull().isNotEmpty();
        assertThat(body[0]).isEqualTo((byte) 'P');
        assertThat(body[1]).isEqualTo((byte) 'K');
        assertThat(exportResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .as("Content-Disposition must carry an attachment filename so the browser triggers a download")
                .startsWith(expectedContentDispositionPrefix);
    }
}
