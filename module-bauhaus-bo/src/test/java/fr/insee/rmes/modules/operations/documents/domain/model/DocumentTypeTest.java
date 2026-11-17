package fr.insee.rmes.modules.operations.documents.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class DocumentTypeTest {

    private static final String DOC_METHOD = "http://ec.europa.eu/eurostat/simsv2/concept/DOC_METHOD";
    private static final String COLLECTION_DOCUMENTS = "http://id.insee.fr/concepts/simsv2fr/COLLECTION_DOCUMENTS";

    @Test
    void should_be_doc_method_when_the_only_rubric_concept_is_doc_method() {
        assertThat(DocumentType.fromRubricConcepts(Set.of(DOC_METHOD))).isEqualTo(DocumentType.DOC_METHOD);
    }

    @Test
    void should_be_collection_documents_when_the_only_rubric_concept_is_collection_documents() {
        assertThat(DocumentType.fromRubricConcepts(Set.of(COLLECTION_DOCUMENTS)))
                .isEqualTo(DocumentType.COLLECTION_DOCUMENTS);
    }

    @Test
    void should_recognise_collection_documents_whatever_the_base_uri_of_the_concept() {
        assertThat(DocumentType.fromRubricConcepts(Set.of("http://bauhaus/concepts/simsv2fr/COLLECTION_DOCUMENTS")))
                .isEqualTo(DocumentType.COLLECTION_DOCUMENTS);
    }

    @Test
    void should_be_other_when_the_rubric_concept_is_another_one() {
        assertThat(DocumentType.fromRubricConcepts(
                        Set.of("http://ec.europa.eu/eurostat/simsv2/concept/COVERAGE_SECTOR")))
                .isEqualTo(DocumentType.OTHER);
    }

    @Test
    void should_be_other_when_the_document_is_attached_to_several_concepts() {
        assertThat(DocumentType.fromRubricConcepts(Set.of(DOC_METHOD, COLLECTION_DOCUMENTS)))
                .isEqualTo(DocumentType.OTHER);
    }

    @Test
    void should_be_other_when_the_document_is_not_attached_to_any_rubric() {
        assertThat(DocumentType.fromRubricConcepts(Set.of())).isEqualTo(DocumentType.OTHER);
    }
}
