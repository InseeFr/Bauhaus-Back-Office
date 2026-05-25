package fr.insee.rmes.modules.operations.msd.infrastructure.legacy;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportGoal;
import fr.insee.rmes.modules.operations.msd.domain.model.ExportedFile;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.MetadataExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.model.commands.SourcesExportRequest;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationExportGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LegacyDocumentationExportGateway implements DocumentationExportGateway {

    private static final String MISSING_DOCUMENTS_HEADER = "X-Missing-Documents";

    private final DocumentationExport documentationExport;
    private final int maxLength;

    public LegacyDocumentationExportGateway(
            DocumentationExport documentationExport,
            @Value("${fr.insee.rmes.bauhaus.filenames.maxlength}") int maxLength) {
        this.documentationExport = documentationExport;
        this.maxLength = maxLength;
    }

    @Override
    public ExportedFile exportMetadataReport(MetadataExportRequest request, ExportGoal goal) throws RmesException {
        ResponseEntity<?> response = documentationExport.exportMetadataReport(
                request.id(),
                request.includeEmptyMas(),
                request.lg1(),
                request.lg2(),
                request.includeDocuments(),
                toLegacyGoal(goal),
                maxLength);
        return toExportedFile(response);
    }

    @Override
    public ExportedFile exportMetadataReportSources(SourcesExportRequest request) throws RmesException {
        ResponseEntity<?> response = documentationExport.exportMetadataReportFiles(
                request.id(),
                request.includeEmptyMas(),
                request.lg1(),
                request.lg2());
        return toExportedFile(response);
    }

    private static String toLegacyGoal(ExportGoal goal) {
        return switch (goal) {
            case RMES -> Constants.GOAL_RMES;
            case COMITE_LABEL -> Constants.GOAL_COMITE_LABEL;
        };
    }

    private static ExportedFile toExportedFile(ResponseEntity<?> response) throws RmesException {
        Object body = response.getBody();
        if (!(body instanceof Resource resource)) {
            throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Empty export body", "");
        }
        HttpHeaders headers = response.getHeaders();
        String filename = readFilename(headers.getContentDisposition());
        String extension = extractExtension(filename);
        String basename = stripExtension(filename, extension);
        String contentType = readContentType(headers.getContentType());
        Set<String> missing = parseMissingDocuments(headers.getFirst(MISSING_DOCUMENTS_HEADER));
        return new ExportedFile(basename, extension, resource, contentType, missing);
    }

    private static String readFilename(ContentDisposition disposition) {
        String filename = (disposition != null) ? disposition.getFilename() : null;
        return (filename != null) ? filename : "export";
    }

    private static String readContentType(MediaType mediaType) {
        return (mediaType != null) ? mediaType.toString() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private static String extractExtension(String filename) {
        int dotIdx = filename.lastIndexOf('.');
        return (dotIdx >= 0) ? filename.substring(dotIdx) : "";
    }

    private static String stripExtension(String filename, String extension) {
        return extension.isEmpty() ? filename : filename.substring(0, filename.length() - extension.length());
    }

    private static Set<String> parseMissingDocuments(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(headerValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
