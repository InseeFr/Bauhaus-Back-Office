package fr.insee.rmes.modules.operations.msd.domain.model;

import java.util.Set;
import org.springframework.core.io.Resource;

public record ExportedFile(
        String filename, String extension, Resource content, String contentType, Set<String> missingDocuments) {
    public ExportedFile {
        if (missingDocuments == null) {
            missingDocuments = Set.of();
        } else {
            missingDocuments = Set.copyOf(missingDocuments);
        }
    }
}
