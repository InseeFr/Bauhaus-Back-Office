package fr.insee.rmes.modules.operations.msd.domain;

import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsConvertedTextNode;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsTextNode;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.SimsMigrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimsMigrationServiceImplTest {

    @Mock
    private SimsMigrationRepository repository;

    private SimsMigrationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SimsMigrationServiceImpl(repository);
    }

    @Test
    void migrateHtmlToMarkdown_callsBulkUpdatePerBatch() throws GenericInternalServerException {
        List<SimsTextNode> nodes = List.of(
                new SimsTextNode("graph1", "uri1", "pred1", "<p>Hello</p>", false, ""),
                new SimsTextNode("graph2", "uri2", "pred2", "<b>World</b>", false, "")
        );
        when(repository.findHtmlTextNodes(anyInt(), eq(0))).thenReturn(nodes);
        when(repository.findHtmlTextNodes(anyInt(), eq(500))).thenReturn(List.of());

        int count = service.migrateHtmlToMarkdown();

        assertThat(count).isEqualTo(2);
        verify(repository, times(1)).bulkUpdateGestionTextNodes(any());
        verify(repository, never()).bulkUpdatePublicationTextNodes(any());
    }

    @Test
    void migrateHtmlToMarkdown_convertsAllNodesToMarkdown() throws GenericInternalServerException {
        List<SimsTextNode> nodes = List.of(
                new SimsTextNode("graph1", "uri1", "pred1", "<p>Hello</p>", false, "")
        );
        when(repository.findHtmlTextNodes(anyInt(), eq(0))).thenReturn(nodes);
        when(repository.findHtmlTextNodes(anyInt(), eq(500))).thenReturn(List.of());

        service.migrateHtmlToMarkdown();

        ArgumentCaptor<List<SimsConvertedTextNode>> captor = ArgumentCaptor.captor();
        verify(repository).bulkUpdateGestionTextNodes(captor.capture());

        List<SimsConvertedTextNode> converted = captor.getValue();
        assertThat(converted).hasSize(1);
        assertThat(converted.getFirst().graph()).isEqualTo("graph1");
        assertThat(converted.getFirst().uri()).isEqualTo("uri1");
        assertThat(converted.getFirst().predicate()).isEqualTo("pred1");
        assertThat(converted.getFirst().needHTML()).isFalse();
        assertThat(converted.getFirst().markdown()).isNotBlank();
        assertThat(converted.getFirst().html()).isNull();
    }

    @Test
    void migratePublicationHtmlToMarkdown_callsBulkUpdatePerBatch() throws GenericInternalServerException {
        List<SimsTextNode> nodes = List.of(
                new SimsTextNode("graph1", "uri1", "pred1", "<p>Hello</p>", true, ""),
                new SimsTextNode("graph2", "uri2", "pred2", "<b>World</b>", false, "")
        );
        when(repository.findPublicationHtmlTextNodes(anyInt(), eq(0))).thenReturn(nodes);
        when(repository.findPublicationHtmlTextNodes(anyInt(), eq(500))).thenReturn(List.of());

        int count = service.migratePublicationHtmlToMarkdown();

        assertThat(count).isEqualTo(2);
        verify(repository, times(1)).bulkUpdatePublicationTextNodes(any());
        verify(repository, never()).bulkUpdateGestionTextNodes(any());
    }

    @Test
    void migratePublicationHtmlToMarkdown_preservesHtmlAndNeedHtmlFlag() throws GenericInternalServerException {
        String html = "<p>Hello</p>";
        List<SimsTextNode> nodes = List.of(
                new SimsTextNode("graph1", "uri1", "pred1", html, true, "")
        );
        when(repository.findPublicationHtmlTextNodes(anyInt(), eq(0))).thenReturn(nodes);
        when(repository.findPublicationHtmlTextNodes(anyInt(), eq(500))).thenReturn(List.of());

        service.migratePublicationHtmlToMarkdown();

        ArgumentCaptor<List<SimsConvertedTextNode>> captor = ArgumentCaptor.captor();
        verify(repository).bulkUpdatePublicationTextNodes(captor.capture());

        SimsConvertedTextNode converted = captor.getValue().getFirst();
        assertThat(converted.needHTML()).isTrue();
        assertThat(converted.html()).isEqualTo(html);
        assertThat(converted.markdown()).isNotBlank();
    }
}