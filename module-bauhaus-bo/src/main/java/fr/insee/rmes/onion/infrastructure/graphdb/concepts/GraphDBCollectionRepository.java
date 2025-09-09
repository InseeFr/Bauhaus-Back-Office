package fr.insee.rmes.onion.infrastructure.graphdb.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.port.serverside.concepts.CollectionRepository;
import fr.insee.rmes.persistance.ontologies.INSEE;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class GraphDBCollectionRepository implements CollectionRepository  {

    private final RepositoryGestion repositoryGestion;
    private final String lg1;
    private final String lg2;

    public GraphDBCollectionRepository(
            RepositoryGestion repositoryGestion,
            @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
            @Value("${fr.insee.rmes.bauhaus.lg2}") String lg2
    ) {
        this.repositoryGestion = repositoryGestion;
        this.lg1 = lg1;
        this.lg2 = lg2;
    }

    @Override
    public String save(Collection collection) throws RmesException {
        Model model = new LinkedHashModel();
        IRI collectionURI = RdfUtils.collectionIRI(collection.getId());
        model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, RdfUtils.conceptGraph());
        model.add(collectionURI, INSEE.IS_VALIDATED, RdfUtils.setLiteralBoolean(collection.getIsValidated()), RdfUtils.conceptGraph());
        model.add(collectionURI, DCTERMS.TITLE, RdfUtils.setLiteralString(collection.getPrefLabelLg1(), this.lg1), RdfUtils.conceptGraph());
        model.add(collectionURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(collection.getCreated()), RdfUtils.conceptGraph());
        model.add(collectionURI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(collection.getContributor()), RdfUtils.conceptGraph());
        model.add(collectionURI, DC.CREATOR, RdfUtils.setLiteralString(collection.getCreator()), RdfUtils.conceptGraph());
        /*Optional*/
        RdfUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, collection.getModified(), model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.TITLE, collection.getPrefLabelLg2(), this.lg2, model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg1(), this.lg1, model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg2(), this.lg2, model, RdfUtils.conceptGraph());

        /*Members*/
        collection.getMembers().forEach(member->{
            IRI memberIRI = RdfUtils.conceptIRI(member);
            model.add(collectionURI, SKOS.MEMBER, memberIRI, RdfUtils.conceptGraph());
        });

        repositoryGestion.loadSimpleObject(collectionURI, model);

        return collection.getId();
    }
}
