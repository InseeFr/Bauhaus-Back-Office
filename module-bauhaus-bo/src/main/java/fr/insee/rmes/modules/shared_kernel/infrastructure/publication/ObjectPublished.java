package fr.insee.rmes.modules.shared_kernel.infrastructure.publication;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

/**
 * Publié après qu'un objet a été copié vers le graphe de publication. Son écouteur,
 * {@link ValidationStatusListener}, passe alors l'objet à l'état « Validated » dans la gestion.
 * <p>
 * Contrairement à {@code SeriesSaved} ou {@code OperationSaved}, l'événement est générique : il ne
 * porte que ce dont l'écouteur de statut a besoin, quel que soit le type d'objet publié. Il vit hors
 * du domaine parce qu'il transporte des types RDF, que la règle ArchUnit sur les dépendances du
 * domaine y interdit.
 *
 * @param subject l'IRI de l'objet publié, dans le graphe de gestion
 * @param managementGraph le graphe de gestion qui porte le statut de l'objet
 */
public record ObjectPublished(IRI subject, Resource managementGraph) {}
