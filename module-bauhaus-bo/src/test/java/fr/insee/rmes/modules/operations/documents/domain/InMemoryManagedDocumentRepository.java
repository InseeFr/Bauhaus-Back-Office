package fr.insee.rmes.modules.operations.documents.domain;

import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentLanguage;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.SimsReference;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.ManagedDocumentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Faux adaptateur en mémoire : les tests passent par le vrai service de domaine, sans mock. */
public final class InMemoryManagedDocumentRepository implements ManagedDocumentRepository {

    private final Map<String, ManagedDocument> documents = new LinkedHashMap<>();
    private final Map<String, List<SimsReference>> simsReferences = new HashMap<>();
    private final Map<String, List<String>> citingTexts = new HashMap<>();
    private int lastId = 999;

    public void clear() {
        documents.clear();
        simsReferences.clear();
        citingTexts.clear();
        lastId = 999;
    }

    public void add(ManagedDocument document) {
        documents.put(document.uri(), document);
    }

    public void citedBy(String uri, String text) {
        citingTexts.computeIfAbsent(uri, _ -> new ArrayList<>()).add(text);
    }

    public void referencedBy(String uri, SimsReference reference) {
        simsReferences.computeIfAbsent(uri, _ -> new ArrayList<>()).add(reference);
    }

    public Optional<ManagedDocument> stored(DocumentKind kind, String id) {
        return Optional.ofNullable(documents.get(uriOf(kind, id)));
    }

    @Override
    public String nextId() {
        return Integer.toString(++lastId);
    }

    @Override
    public String uriOf(DocumentKind kind, String id) {
        return "http://bauhaus/documents/" + (kind == DocumentKind.LINK ? "page/" : "document/") + id;
    }

    @Override
    public List<ManagedDocument> findAll() {
        return List.copyOf(documents.values());
    }

    @Override
    public Optional<ManagedDocument> find(DocumentKind kind, String id) {
        return stored(kind, id);
    }

    @Override
    public List<SimsReference> findSimsReferences(DocumentKind kind, String id) {
        return simsReferences.getOrDefault(uriOf(kind, id), List.of());
    }

    @Override
    public boolean isLabelUsedByAnother(String label, DocumentLanguage language, String excludedUri) {
        return documents.values().stream()
                .filter(document -> !document.uri().equals(excludedUri))
                .map(ManagedDocument::form)
                .map(form -> language == DocumentLanguage.FIRST ? form.labelLg1() : form.labelLg2())
                .anyMatch(label::equals);
    }

    @Override
    public Optional<String> findUriByUrl(String url) {
        return documents.values().stream()
                .filter(document -> url.equalsIgnoreCase(document.form().url()))
                .map(ManagedDocument::uri)
                .findFirst();
    }

    @Override
    public List<String> findSimsTextsCiting(String uri) {
        return citingTexts.getOrDefault(uri, List.of());
    }

    @Override
    public void save(ManagedDocument document) {
        documents.put(document.uri(), document);
    }

    @Override
    public void delete(String uri) {
        documents.remove(uri);
    }

    public static DocumentForm form(String labelLg1, String labelLg2, String url) {
        return new DocumentForm(labelLg1, labelLg2, "description", null, "2026-09-24", "fr", url);
    }
}
