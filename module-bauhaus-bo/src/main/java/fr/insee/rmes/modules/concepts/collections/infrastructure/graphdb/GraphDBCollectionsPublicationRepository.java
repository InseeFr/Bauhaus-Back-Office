package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;


import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.concepts.collections.domain.model.PublishedCollection;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsPublicationRepository;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.stereotype.Repository;

import java.util.Set;

@ServerSideAdaptor
@Repository
public class GraphDBCollectionsPublicationRepository implements CollectionsPublicationRepository {

    private final GraphDBCollectionProperties graphDBCollectionProperties;

    public GraphDBCollectionsPublicationRepository(GraphDBCollectionProperties graphDBCollectionProperties) {
        this.graphDBCollectionProperties = graphDBCollectionProperties;
    }

    @Override
    public void publish(Set<PublishedCollection> publishedCollections) {
        Model model = new LinkedHashModel();
        for (PublishedCollection publishedCollection : publishedCollections) {
            generateCollectionRDF(model, publishedCollection);
        }
    }

    private void generateCollectionRDF(Model model, PublishedCollection publishedCollection) {
        IRI collectionURI = graphDBCollectionProperties.getResourceIRI(publishedCollection.id().value());
        Resource graph = graphDBCollectionProperties.getResourceGraph();

        model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, graph);

        model.add(collectionURI, DCTERMS.TITLE, RdfUtils.setLiteralString(publishedCollection.prefLabel().value(),
                publishedCollection.prefLabel().lang().name()), graph);
        model.add(collectionURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(publishedCollection.created().toString()), graph);

        /*Optional*/
        RdfUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, publishedCollection.modified().get().toString(), model, graph);
        publishedCollection.alternativeLabels().forEach(alternativeLabel->RdfUtils.addTripleString(collectionURI, DCTERMS.TITLE, alternativeLabel.value(), alternativeLabel.lang().name(), model, graph));
        publishedCollection.descriptions().forEach(description->RdfUtils.addTripleString(collectionURI, DCTERMS.TITLE, description.value(), description.lang().name(), model, graph));

    }
}
