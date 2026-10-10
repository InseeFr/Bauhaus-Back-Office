package fr.insee.rmes.modules.operations.documents.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import java.util.Optional;
import java.util.Set;

@ServerSidePort
public interface DocumentDescriptionRepository {

    Optional<DocumentMetadata> findMetadata(DocumentKind kind, String id) throws RmesException;

    /** Concepts des rubriques de rapport qualité qui citent le document, dédoublonnés. */
    Set<String> findRubricConcepts(String documentUri) throws RmesException;
}
