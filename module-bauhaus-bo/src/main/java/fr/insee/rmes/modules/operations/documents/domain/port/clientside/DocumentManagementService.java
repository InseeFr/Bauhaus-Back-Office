package fr.insee.rmes.modules.operations.documents.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.StoredFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDetails;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.StoredFile;
import fr.insee.rmes.modules.operations.documents.domain.model.UploadedFile;
import java.util.List;
import java.util.Optional;

@ClientSidePort
public interface DocumentManagementService {

    List<ManagedDocument> getAll() throws RmesException;

    DocumentDetails get(DocumentKind kind, String id) throws RmesException, DocumentNotFoundException;

    /** @return l'identifiant du document créé */
    String createDocument(DocumentForm form, UploadedFile file) throws RmesException, DocumentRuleViolationException;

    /** @return l'identifiant du lien créé */
    String createLink(DocumentForm form) throws RmesException, DocumentRuleViolationException;

    void update(DocumentKind kind, String id, DocumentForm form)
            throws RmesException, DocumentNotFoundException, DocumentRuleViolationException;

    /** @return la nouvelle URL du document, vide quand le fichier garde son nom */
    Optional<String> replaceFile(String id, UploadedFile file)
            throws RmesException, DocumentNotFoundException, DocumentRuleViolationException;

    void delete(DocumentKind kind, String id)
            throws RmesException, DocumentNotFoundException, DocumentRuleViolationException;

    StoredFile download(String id) throws RmesException, DocumentNotFoundException, StoredFileNotFoundException;
}
