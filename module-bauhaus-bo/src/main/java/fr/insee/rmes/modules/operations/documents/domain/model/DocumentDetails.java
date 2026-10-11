package fr.insee.rmes.modules.operations.documents.domain.model;

import java.util.List;

/** Un document ou un lien, avec les rubriques de rapports qualité qui le citent. */
public record DocumentDetails(ManagedDocument document, List<SimsReference> sims) {}
