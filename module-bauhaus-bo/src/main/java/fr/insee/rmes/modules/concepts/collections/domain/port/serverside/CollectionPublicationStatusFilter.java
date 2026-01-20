package fr.insee.rmes.modules.concepts.collections.domain.port.serverside;

public enum CollectionPublicationStatusFilter {
    ALL,
    UNPUBLISHED;

    public boolean isUnpublished(){
        return this == CollectionPublicationStatusFilter.UNPUBLISHED;
    }
}
