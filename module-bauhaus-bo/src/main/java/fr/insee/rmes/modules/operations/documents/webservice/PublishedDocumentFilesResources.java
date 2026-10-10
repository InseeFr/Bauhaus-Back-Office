package fr.insee.rmes.modules.operations.documents.webservice;

import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.commons.security.PublicEndpoint;
import fr.insee.rmes.modules.commons.webservice.ApiError;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.PublishedFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.PublishedFile;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.PublishedDocumentFileService;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Diffusion des fichiers publiés, pour ne plus dépendre d'insee.fr (magma-fusion#26). */
@RestController
@RequestMapping("/documents")
@ConditionalOnModule("operations")
public class PublishedDocumentFilesResources {

    private static final Logger logger = LoggerFactory.getLogger(PublishedDocumentFilesResources.class);

    private final PublishedDocumentFileService publishedDocumentFileService;

    public PublishedDocumentFilesResources(PublishedDocumentFileService publishedDocumentFileService) {
        this.publishedDocumentFileService = publishedDocumentFileService;
    }

    /**
     * Le fichier est servi « inline » avec le type de son extension, pour que le navigateur l'ouvre.
     * Il l'est depuis l'origine de l'API : un HTML diffusé ne doit pas pouvoir y exécuter de script,
     * d'où le bac à sable CSP, et nosniff pour que le navigateur s'en tienne au type annoncé.
     */
    @PublicEndpoint
    @GetMapping(value = "/fichier/{fichier}", produces = "*/*")
    public ResponseEntity<Resource> getPublishedFile(@PathVariable("fichier") String fileName)
            throws PublishedFileNotFoundException {
        PublishedFile file = publishedDocumentFileService.getPublishedFile(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mediaType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(file.name(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header("Content-Security-Policy", "sandbox")
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(file.content()));
    }

    @ExceptionHandler(PublishedFileNotFoundException.class)
    public ResponseEntity<ApiError> handlePublishedFileNotFound(PublishedFileNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    /** Stockage illisible (MinIO en panne…) : ni un 404 mensonger, ni le détail interne à un anonyme. */
    @ExceptionHandler(RmesFileException.class)
    public ResponseEntity<ApiError> handleUnreadableStorage(RmesFileException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "The file storage could not be read"));
    }
}
