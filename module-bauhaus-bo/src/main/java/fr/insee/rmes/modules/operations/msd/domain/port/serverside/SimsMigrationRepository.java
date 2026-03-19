package fr.insee.rmes.modules.operations.msd.domain.port.serverside;

import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsTextNode;

import java.util.List;

public interface SimsMigrationRepository {
    List<SimsTextNode> findAllHtmlTextNodes() throws GenericInternalServerException;
    void updateTextNodeValue(String graph, String uri, String markdownValue) throws GenericInternalServerException;

    List<SimsTextNode> findAllPublicationHtmlTextNodes() throws GenericInternalServerException;
    void updatePublicationTextNodeWithMarkdownAndHtml(String graph, String uri, String markdown, String html) throws GenericInternalServerException;
}