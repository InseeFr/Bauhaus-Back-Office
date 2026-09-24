package fr.insee.rmes.modules.operations.documents;

import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentDescriptionService;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentDescriptionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperationsDocumentsConfiguration {
    @Bean
    DocumentDescriptionService documentDescriptionService(DocumentDescriptionRepository documentDescriptionRepository) {
        return new DomainDocumentDescriptionService(documentDescriptionRepository);
    }
}
