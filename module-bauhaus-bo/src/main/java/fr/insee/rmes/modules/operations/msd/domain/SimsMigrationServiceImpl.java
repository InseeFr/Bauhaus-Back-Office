package fr.insee.rmes.modules.operations.msd.domain;

import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsConvertedTextNode;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsTextNode;
import fr.insee.rmes.modules.operations.msd.domain.port.clientside.SimsMigrationService;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.SimsMigrationRepository;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SimsMigrationServiceImpl implements SimsMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(SimsMigrationServiceImpl.class);

    private final SimsMigrationRepository repository;

    public SimsMigrationServiceImpl(SimsMigrationRepository repository) {
        this.repository = repository;
    }

    private static final int BATCH_SIZE = 500;

    @Override
    public int migrateHtmlToMarkdown() throws GenericInternalServerException {
        int offset = 0;
        int totalCount = 0;

        logger.info("Starting HTML to Markdown migration (gestion), batch size: {}", BATCH_SIZE);
        while (true) {
            long tFetch = System.currentTimeMillis();
            List<SimsTextNode> batch = repository.findHtmlTextNodes(BATCH_SIZE, offset);
            logger.info("Gestion SPARQL fetch at offset {} took {}ms, {} nodes returned", offset, System.currentTimeMillis() - tFetch, batch.size());
            if (batch.isEmpty()) break;

            long tConvert = System.currentTimeMillis();
            List<SimsConvertedTextNode> converted = batch.stream()
                    .map(node -> new SimsConvertedTextNode(node.graph(), node.uri(), node.predicate(), false, XhtmlToMarkdownUtils.xhtmlToMarkdown(node.value()), null, node.lang()))
                    .toList();
            logger.info("Gestion Flexmark conversion took {}ms for {} nodes", System.currentTimeMillis() - tConvert, batch.size());

            long tUpdate = System.currentTimeMillis();
            repository.bulkUpdateGestionTextNodes(converted);
            logger.info("Gestion bulkUpdate took {}ms for {} nodes", System.currentTimeMillis() - tUpdate, batch.size());

            totalCount += batch.size();
            logger.info("Gestion batch at offset {} done, total migrated so far: {}", offset, totalCount);
            offset += BATCH_SIZE;
        }
        logger.info("HTML to Markdown migration (gestion) finished: {} nodes migrated", totalCount);
        return totalCount;
    }

    @Override
    public int migratePublicationHtmlToMarkdown() throws GenericInternalServerException {
        int offset = 0;
        int totalCount = 0;

        logger.info("Starting HTML to Markdown migration (publication), batch size: {}", BATCH_SIZE);
        while (true) {
            long tFetch = System.currentTimeMillis();
            List<SimsTextNode> batch = repository.findPublicationHtmlTextNodes(BATCH_SIZE, offset);
            logger.info("Publication SPARQL fetch at offset {} took {}ms, {} nodes returned", offset, System.currentTimeMillis() - tFetch, batch.size());
            if (batch.isEmpty()) break;

            long tConvert = System.currentTimeMillis();
            List<SimsConvertedTextNode> converted = batch.stream()
                    .map(node -> new SimsConvertedTextNode(node.graph(), node.uri(), node.predicate(), node.needHTML(), XhtmlToMarkdownUtils.xhtmlToMarkdown(node.value()), node.value(), node.lang()))
                    .toList();
            logger.info("Publication Flexmark conversion took {}ms for {} nodes", System.currentTimeMillis() - tConvert, batch.size());

            long tUpdate = System.currentTimeMillis();
            repository.bulkUpdatePublicationTextNodes(converted);
            logger.info("Publication bulkUpdate took {}ms for {} nodes", System.currentTimeMillis() - tUpdate, batch.size());

            totalCount += batch.size();
            logger.info("Publication batch at offset {} done, total migrated so far: {}", offset, totalCount);
            offset += BATCH_SIZE;
        }
        logger.info("HTML to Markdown migration (publication) finished: {} nodes migrated", totalCount);
        return totalCount;
    }
}