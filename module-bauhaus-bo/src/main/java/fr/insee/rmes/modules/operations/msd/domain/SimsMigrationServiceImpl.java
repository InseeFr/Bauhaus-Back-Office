package fr.insee.rmes.modules.operations.msd.domain;

import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsTextNode;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.SimsMigrationService;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.SimsMigrationRepository;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

import java.util.List;

public class SimsMigrationServiceImpl implements SimsMigrationService {

    private final SimsMigrationRepository repository;

    public SimsMigrationServiceImpl(SimsMigrationRepository repository) {
        this.repository = repository;
    }

    @Override
    public int migrateHtmlToMarkdown() throws GenericInternalServerException {
        List<SimsTextNode> nodes = repository.findAllHtmlTextNodes();
        for (SimsTextNode node : nodes) {
            String markdown = XhtmlToMarkdownUtils.xhtmlToMarkdown(node.value());
            repository.updateTextNodeValue(node.graph(), node.uri(), markdown);
        }
        return nodes.size();
    }

    @Override
    public int migratePublicationHtmlToMarkdown() throws GenericInternalServerException {
        List<SimsTextNode> nodes = repository.findAllPublicationHtmlTextNodes();
        for (SimsTextNode node : nodes) {
            String markdown = XhtmlToMarkdownUtils.xhtmlToMarkdown(node.value());
            repository.updatePublicationTextNodeWithMarkdownAndHtml(node.graph(), node.uri(), markdown, node.value());
        }
        return nodes.size();
    }
}