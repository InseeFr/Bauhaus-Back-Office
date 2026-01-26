package fr.insee.rmes.modules.concepts.collections.domain.port.serverside;

import fr.insee.rmes.modules.concepts.collections.domain.model.PublishedCollection;

import java.util.Set;

public interface CollectionsPublicationRepository {

    void publish(Set<PublishedCollection> publishedCollections);
}
