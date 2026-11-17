package fr.insee.rmes.modules.operations.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDescription;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentType;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DomainDocumentDescriptionServiceTest {

    private static final String DOC_METHOD = "http://ec.europa.eu/eurostat/simsv2/concept/DOC_METHOD";

    private final InMemoryDocumentDescriptionRepository repository = new InMemoryDocumentDescriptionRepository();
    private final DomainDocumentDescriptionService service = new DomainDocumentDescriptionService(repository);

    @Test
    void should_describe_a_document_with_the_type_computed_from_its_rubrics() throws Exception {
        DocumentMetadata metadata = metadata("http://bauhaus/documents/document/1070");
        repository.add(DocumentKind.DOCUMENT, "1070", metadata, Set.of(DOC_METHOD));

        DocumentDescription description = service.getDescription(DocumentKind.DOCUMENT, "1070");

        assertThat(description).isEqualTo(new DocumentDescription(metadata, DocumentType.DOC_METHOD));
    }

    @Test
    void should_describe_a_link_from_the_link_identifiers() throws Exception {
        DocumentMetadata document = metadata("http://bauhaus/documents/document/12");
        DocumentMetadata link = metadata("http://bauhaus/documents/page/12");
        repository.add(DocumentKind.DOCUMENT, "12", document, Set.of(DOC_METHOD));
        repository.add(DocumentKind.LINK, "12", link, Set.of());

        DocumentDescription description = service.getDescription(DocumentKind.LINK, "12");

        assertThat(description).isEqualTo(new DocumentDescription(link, DocumentType.OTHER));
    }

    @Test
    void should_fail_when_the_document_does_not_exist() {
        assertThatThrownBy(() -> service.getDescription(DocumentKind.DOCUMENT, "404"))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessage("Document 404 doesn't exist");
    }

    private static DocumentMetadata metadata(String uri) {
        return new DocumentMetadata(
                uri,
                List.of(LocalisedLabel.ofDefaultLanguage("Note technique")),
                List.of(),
                LocalDate.of(2026, 4, 9),
                "fr",
                new FileSize(127_000),
                "https://www.insee.fr/fichier.pdf");
    }
}
