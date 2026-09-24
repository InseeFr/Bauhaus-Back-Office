package fr.insee.rmes.modules.operations.documents.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentLanguage;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import java.util.List;
import java.util.Optional;

@ServerSidePort
public interface ManagedDocumentRepository {

    /** Identifiant suivant, commun aux documents et aux liens. */
    String nextId() throws RmesException;

    String uriOf(DocumentKind kind, String id);

    List<ManagedDocument> findAll() throws RmesException;

    Optional<ManagedDocument> find(DocumentKind kind, String id) throws RmesException;

    List<SimsReference> findSimsReferences(DocumentKind kind, String id) throws RmesException;

    /** Un autre document ou lien que {@code excludedUri} porte-t-il déjà ce libellé ? */
    boolean isLabelUsedByAnother(String label, DocumentLanguage language, String excludedUri) throws RmesException;

    /** Document ou lien dont l'URL est celle-ci, à la casse près. */
    Optional<String> findUriByUrl(String url) throws RmesException;

    /** Textes de rubriques de rapports qualité qui citent le document ou le lien. */
    List<String> findSimsTextsCiting(String uri) throws RmesException;

    /** Écrit le document ou le lien en remplaçant tout ce qui était stocké pour lui. */
    void save(ManagedDocument document) throws RmesException;

    void delete(String uri) throws RmesException;
}
