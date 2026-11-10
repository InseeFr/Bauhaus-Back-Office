package fr.insee.rmes.modules.operations.documents.domain;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException.Violation;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.StoredFileNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentDetails;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentLanguage;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import fr.insee.rmes.modules.operations.documents.domain.model.StoredFile;
import fr.insee.rmes.modules.operations.documents.domain.model.UploadedFile;
import fr.insee.rmes.modules.operations.documents.domain.port.clientside.DocumentManagementService;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentFileStorage;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.ManagedDocumentRepository;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.SimsOwnersLookup;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Gestion des documents (un fichier déposé) et des liens (une page web). Les deux partagent une
 * même suite d'identifiants, les mêmes libellés uniques et la même interdiction de supprimer ce
 * qu'un rapport qualité cite.
 */
public class DomainDocumentManagementService implements DocumentManagementService {

    /** Un nom de fichier sûr pour toutes les URL et tous les stockages : pas d'espace, pas d'accent. */
    private static final Pattern VALID_FILE_NAME = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z]+$");

    private final ManagedDocumentRepository repository;
    private final DocumentFileStorage storage;
    private final SimsOwnersLookup simsOwners;
    private final Set<String> allowedExtensions;

    public DomainDocumentManagementService(
            ManagedDocumentRepository repository,
            DocumentFileStorage storage,
            SimsOwnersLookup simsOwners,
            Set<String> allowedExtensions) {
        this.repository = repository;
        this.storage = storage;
        this.simsOwners = simsOwners;
        this.allowedExtensions = allowedExtensions;
    }

    @Override
    public List<ManagedDocument> getAll() throws RmesException {
        return repository.findAll();
    }

    @Override
    public DocumentDetails get(DocumentKind kind, String id) throws RmesException, DocumentNotFoundException {
        ManagedDocument document = find(kind, id);
        List<SimsReference> sims = new ArrayList<>();
        for (SimsReference reference : repository.findSimsReferences(kind, id)) {
            sims.add(new SimsReference(
                    reference.simsId(),
                    reference.labelLg1(),
                    reference.labelLg2(),
                    reference.rubricId(),
                    simsOwners.ownersOf(reference.simsId())));
        }
        return new DocumentDetails(document, sims);
    }

    @Override
    public String createDocument(DocumentForm form, UploadedFile file)
            throws RmesException, DocumentRuleViolationException {
        checkFileName(file.name());
        String id = repository.nextId();
        String uri = repository.uriOf(DocumentKind.DOCUMENT, id);
        checkLabels(form, uri);
        checkFileNameIsFree(file.name());

        String url = storage.write(file.name(), file.content());
        repository.save(new ManagedDocument(id, DocumentKind.DOCUMENT, uri, form.withUrl(url), sizeOf(file)));
        return id;
    }

    @Override
    public String createLink(DocumentForm form) throws RmesException, DocumentRuleViolationException {
        String id = repository.nextId();
        String uri = repository.uriOf(DocumentKind.LINK, id);
        checkLabels(form, uri);
        checkLinkUrl(form.url(), uri);

        repository.save(new ManagedDocument(id, DocumentKind.LINK, uri, form, null));
        return id;
    }

    /**
     * Les libellés, descriptions, date et langue viennent du formulaire. L'URL d'un document et sa
     * taille restent celles du fichier déposé : seul le remplacement du fichier les change.
     */
    @Override
    public void update(DocumentKind kind, String id, DocumentForm form)
            throws RmesException, DocumentNotFoundException, DocumentRuleViolationException {
        ManagedDocument stored = find(kind, id);
        checkLabels(form, stored.uri());

        DocumentForm updated =
                kind == DocumentKind.DOCUMENT ? form.withUrl(stored.form().url()) : form;
        repository.save(stored.withForm(updated));
    }

    @Override
    public Optional<String> replaceFile(String id, UploadedFile file)
            throws RmesException, DocumentNotFoundException, DocumentRuleViolationException {
        ManagedDocument stored = find(DocumentKind.DOCUMENT, id);
        checkExtensionIsAllowed(file.name());
        checkFileName(file.name());

        String currentName = storage.fileNameOf(stored.form().url());
        boolean sameName = currentName.equals(file.name());
        if (!sameName) {
            checkFileNameIsFree(file.name());
        }

        String url = storage.write(file.name(), file.content());
        if (!sameName) {
            storage.delete(currentName);
        }
        repository.save(stored.withFile(url, sizeOf(file)));
        return sameName ? Optional.empty() : Optional.of(url);
    }

    @Override
    public void delete(DocumentKind kind, String id)
            throws RmesException, DocumentNotFoundException, DocumentRuleViolationException {
        ManagedDocument stored = find(kind, id);
        List<String> citingTexts = repository.findSimsTextsCiting(stored.uri());
        if (!citingTexts.isEmpty()) {
            throw new DocumentRuleViolationException(
                    Violation.REFERENCED_BY_SIMS,
                    "The document " + stored.uri() + " cannot be deleted because it is referred to by "
                            + citingTexts.size() + " sims, including: " + citingTexts.getFirst(),
                    String.join(", ", citingTexts));
        }
        if (kind == DocumentKind.DOCUMENT) {
            storage.delete(storage.fileNameOf(stored.form().url()));
        }
        repository.delete(stored.uri());
    }

    @Override
    public StoredFile download(String id) throws RmesException, DocumentNotFoundException, StoredFileNotFoundException {
        ManagedDocument stored = find(DocumentKind.DOCUMENT, id);
        String fileName = storage.fileNameOf(stored.form().url());
        if (!storage.exists(fileName)) {
            throw new StoredFileNotFoundException(fileName);
        }
        return new StoredFile(fileName, storage.read(fileName));
    }

    private ManagedDocument find(DocumentKind kind, String id) throws RmesException, DocumentNotFoundException {
        return repository.find(kind, id).orElseThrow(() -> new DocumentNotFoundException(kind, id));
    }

    private static FileSize sizeOf(UploadedFile file) {
        return new FileSize(file.size());
    }

    private void checkLabels(DocumentForm form, String uri) throws RmesException, DocumentRuleViolationException {
        if (isLabelUsed(form.labelLg1(), DocumentLanguage.FIRST, uri)) {
            throw new DocumentRuleViolationException(
                    Violation.LABEL_LG1_ALREADY_USED,
                    "This labelLg1 is already used by another document or link.",
                    form.labelLg1());
        }
        if (isLabelUsed(form.labelLg2(), DocumentLanguage.SECOND, uri)) {
            throw new DocumentRuleViolationException(
                    Violation.LABEL_LG2_ALREADY_USED,
                    "This labelLg2 is already used by another document or link.",
                    form.labelLg2());
        }
    }

    /** Un libellé absent n'a rien à comparer : les libellés sont facultatifs. */
    private boolean isLabelUsed(@Nullable String label, DocumentLanguage language, String uri) throws RmesException {
        return label != null && !label.isEmpty() && repository.isLabelUsedByAnother(label, language, uri);
    }

    private void checkLinkUrl(@Nullable String url, String uri) throws RmesException, DocumentRuleViolationException {
        if (url == null || url.isEmpty()) {
            throw new DocumentRuleViolationException(
                    Violation.LINK_EMPTY_URL, "A link must have a non-empty url. ", uri);
        }
        try {
            // URI.create rejette une URI syntaxiquement invalide, toURL une URI relative.
            URI.create(url).toURL();
        } catch (MalformedURLException | IllegalArgumentException _) {
            throw new DocumentRuleViolationException(Violation.LINK_BAD_URL, "A link must be a valid url. ", url);
        }
        Optional<String> owner = repository.findUriByUrl(url);
        if (owner.isPresent() && !owner.get().equals(uri)) {
            throw new DocumentRuleViolationException(
                    Violation.LINK_URL_ALREADY_USED, "This url is already referenced by another link.", owner.get());
        }
    }

    private static void checkFileName(String fileName) throws DocumentRuleViolationException {
        if (fileName.isEmpty()) {
            throw new DocumentRuleViolationException(Violation.FILE_EMPTY_NAME, "Empty fileName", "fileName is empty");
        }
        if (!VALID_FILE_NAME.matcher(fileName).matches()) {
            throw new DocumentRuleViolationException(
                    Violation.FILE_FORBIDDEN_CHARACTERS,
                    "FileName contains forbidden characters, please use only Letters, Numbers, Underscores and Hyphens",
                    fileName);
        }
    }

    private void checkExtensionIsAllowed(String fileName) throws DocumentRuleViolationException {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!allowedExtensions.contains(extension)) {
            throw new DocumentRuleViolationException(
                    Violation.FILE_EXTENSION_NOT_ALLOWED, "Invalid File Extension", fileName);
        }
    }

    /** Le stockage fait foi : un fichier du même nom appartient déjà à un autre document. */
    private void checkFileNameIsFree(String fileName) throws DocumentRuleViolationException {
        if (storage.exists(fileName)) {
            throw new DocumentRuleViolationException(
                    Violation.FILE_ALREADY_EXISTS, "There is already a document with that name.", fileName);
        }
    }
}
