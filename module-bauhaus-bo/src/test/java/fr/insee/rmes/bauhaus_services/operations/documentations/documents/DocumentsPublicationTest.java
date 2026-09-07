package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.domain.model.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentsPublicationTest {

    @Mock
    private DocumentsUtils docUtils;

    @Mock
    private fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations filesOperations;

    @InjectMocks
    private DocumentsPublication documentsPublication;

    @Test
    void shouldCollectIdsOfDocumentsMissingFromStorageWithoutCopyingAnything() throws RmesException {
        JSONArray documents = new JSONArray()
                .put(new JSONObject().put("id", "1").put("url", "file:///gestion/doc1.pdf"))
                .put(new JSONObject().put("id", "2").put("url", "file:///gestion/doc2.pdf"))
                .put(new JSONObject().put("id", "3").put("url", "file:///gestion/doc3.pdf"));
        when(docUtils.getListDocumentSims("sims1")).thenReturn(documents);
        when(docUtils.existsInStorage("doc1.pdf")).thenReturn(false);
        when(docUtils.existsInStorage("doc2.pdf")).thenReturn(true);
        when(docUtils.existsInStorage("doc3.pdf")).thenReturn(false);

        Set<String> missing = documentsPublication.findMissingDocuments("sims1");

        assertThat(missing).containsExactlyInAnyOrder("1", "3");
        verify(filesOperations, never()).copy(any(Document.class), any(Document.class));
    }

    @Test
    void shouldReturnEmptySetWhenEveryDocumentExistsInStorage() throws RmesException {
        JSONArray documents = new JSONArray()
                .put(new JSONObject().put("id", "1").put("url", "file:///gestion/doc1.pdf"))
                .put(new JSONObject().put("id", "2").put("url", "file:///gestion/doc2.pdf"));
        when(docUtils.getListDocumentSims("sims1")).thenReturn(documents);
        when(docUtils.existsInStorage("doc1.pdf")).thenReturn(true);
        when(docUtils.existsInStorage("doc2.pdf")).thenReturn(true);

        assertThat(documentsPublication.findMissingDocuments("sims1")).isEmpty();
    }
}
