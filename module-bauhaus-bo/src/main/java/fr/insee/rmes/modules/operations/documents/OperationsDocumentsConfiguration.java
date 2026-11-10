package fr.insee.rmes.modules.operations.documents;

import fr.insee.rmes.DocumentsStorageProperties;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentManagementService;
import fr.insee.rmes.modules.operations.documents.domain.DomainPublishedDocumentFileService;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentManagementService;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.PublishedDocumentFileService;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentDescriptionRepository;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentFileStorage;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.ManagedDocumentRepository;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.SimsOwnersLookup;
import fr.insee.rmes.modules.operations.documents.infrastructure.storage.FilesOperationsDocumentFileStorage;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * Fichiers des documents en gestion : lus et écrits sous {@code storage.directoryGestion} ; l'URL
     * enregistrée sur le document garde le chemin historique du stockage de gestion.
     */
    @Bean
    DocumentFileStorage documentFileStorage(
            FilesOperations filesOperations,
            StorageProperties storageProperties,
            DocumentsStorageProperties documentsStorageProperties) {
        return new FilesOperationsDocumentFileStorage(
                filesOperations, storageProperties.directoryGestion(), documentsStorageProperties.storageGestion());
    }

    /** Les extensions acceptées au remplacement d'un fichier : {@code fr.insee.rmes.bauhaus.extensions}. */
    @Bean
    DocumentManagementService documentManagementService(
            ManagedDocumentRepository managedDocumentRepository,
            DocumentFileStorage documentFileStorage,
            SimsOwnersLookup simsOwnersLookup,
            @Value("${fr.insee.rmes.bauhaus.extensions}") String extensions) {
        return new DomainDocumentManagementService(
                managedDocumentRepository,
                documentFileStorage,
                simsOwnersLookup,
                Arrays.stream(extensions.split(","))
                        .map(extension -> extension.trim().toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet()));
    }
}
