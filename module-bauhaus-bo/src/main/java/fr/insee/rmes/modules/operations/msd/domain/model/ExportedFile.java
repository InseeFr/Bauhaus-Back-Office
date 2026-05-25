package fr.insee.rmes.modules.operations.msd.domain.model;

import org.springframework.core.io.Resource;

import java.util.Set;

public record ExportedFile(
        String filename,
        String extension,
        Resource content,
        String contentType,
        Set<String> missingDocuments
) {
    public ExportedFile {
        if (missingDocuments == null) {
            missingDocuments = Set.of();
        } else {
            missingDocuments = Set.copyOf(missingDocuments);
        }
    }
}
