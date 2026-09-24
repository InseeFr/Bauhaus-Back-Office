package fr.insee.rmes.modules.operations.documents;

import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.DomainPublishedDocumentFileService;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.PublishedDocumentFileService;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentDescriptionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperationsDocumentsConfiguration {
    @Bean
    DocumentDescriptionService documentDescriptionService(DocumentDescriptionRepository documentDescriptionRepository) {
        return new DomainDocumentDescriptionService(documentDescriptionRepository);
    }

    /** Sert les fichiers là où la publication des rapports qualité les copie. */
    @Bean
    PublishedDocumentFileService publishedDocumentFileService(
            FilesOperations filesOperations, StorageProperties storageProperties) {
        return new DomainPublishedDocumentFileService(filesOperations, storageProperties.directoryPublication());
    }
}
