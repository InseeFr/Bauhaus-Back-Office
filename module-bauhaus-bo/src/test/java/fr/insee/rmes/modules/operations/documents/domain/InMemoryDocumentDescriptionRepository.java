package fr.insee.rmes.modules.operations.documents.domain;

import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentMetadata;
import fr.insee.rmes.modules.operations.documents.domain.port.serverside.DocumentDescriptionRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Faux adaptateur en mémoire : les tests passent par le vrai service de domaine, sans mock. */
public final class InMemoryDocumentDescriptionRepository implements DocumentDescriptionRepository {

    private final Map<String, DocumentMetadata> documents = new HashMap<>();
    private final Map<String, Set<String>> concepts = new HashMap<>();

    public void add(DocumentKind kind, String id, DocumentMetadata metadata, Set<String> rubricConcepts) {
        documents.put(kind + "/" + id, metadata);
        concepts.put(metadata.uri(), rubricConcepts);
    }

    public void clear() {
        documents.clear();
        concepts.clear();
    }

    @Override
    public Optional<DocumentMetadata> findMetadata(DocumentKind kind, String id) {
        return Optional.ofNullable(documents.get(kind + "/" + id));
    }

    @Override
    public Set<String> findRubricConcepts(String documentUri) {
        return concepts.getOrDefault(documentUri, Set.of());
    }
}
