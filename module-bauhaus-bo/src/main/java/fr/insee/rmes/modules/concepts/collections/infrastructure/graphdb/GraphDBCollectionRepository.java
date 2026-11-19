package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.time.LocalDateTime;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.stereotype.Repository;

@Repository
public class GraphDBCollectionRepository implements CollectionRepository {

    private final RepositoryGestion repositoryGestion;
    private final BauhausLanguagesProperties languages;
    private final BauhausIriFactory iriFactory;

    public GraphDBCollectionRepository(
            RepositoryGestion repositoryGestion, BauhausLanguagesProperties languages, BauhausIriFactory iriFactory) {
        this.repositoryGestion = repositoryGestion;
        this.languages = languages;
        this.iriFactory = iriFactory;
    }

    @Override
    public String save(Collection collection) throws RmesException {
        Model model = new LinkedHashModel();
        IRI collectionURI = iriFactory.collection(collection.getId());
        model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, iriFactory.conceptGraph());
        model.add(
                collectionURI,
                INSEE.VALIDATION_STATE,
                RdfTriples.string(
                        Boolean.TRUE.equals(collection.getIsValidated())
                                ? ValidationStatus.VALIDATED
                                : ValidationStatus.UNPUBLISHED),
                iriFactory.conceptGraph());
        model.add(
                collectionURI,
                DCTERMS.TITLE,
                RdfTriples.string(collection.getPrefLabelLg1(), languages.lg1()),
                iriFactory.conceptGraph());
        model.add(
                collectionURI,
                DCTERMS.CREATED,
                RdfTriples.dateTime(collection.getCreated()),
                iriFactory.conceptGraph());
        RdfTriples.addDate(
                collectionURI, DCTERMS.MODIFIED, LocalDateTime.now().toString(), model, iriFactory.conceptGraph());
        model.add(
                collectionURI,
                DC.CONTRIBUTOR,
                RdfTriples.string(collection.getContributor()),
                iriFactory.conceptGraph());
        model.add(collectionURI, DC.CREATOR, RdfTriples.string(collection.getCreator()), iriFactory.conceptGraph());
        /*Optional*/
        RdfTriples.addDateTime(
                collectionURI, DCTERMS.MODIFIED, collection.getModified(), model, iriFactory.conceptGraph());
        RdfTriples.addString(
                collectionURI,
                DCTERMS.TITLE,
                collection.getPrefLabelLg2(),
                languages.lg2(),
                model,
                iriFactory.conceptGraph());
        RdfTriples.addString(
                collectionURI,
                DCTERMS.DESCRIPTION,
                collection.getDescriptionLg1(),
                languages.lg1(),
                model,
                iriFactory.conceptGraph());
        RdfTriples.addString(
                collectionURI,
                DCTERMS.DESCRIPTION,
                collection.getDescriptionLg2(),
                languages.lg2(),
                model,
                iriFactory.conceptGraph());

        /*Members*/
        collection.getMembers().forEach(member -> {
            IRI memberIRI = iriFactory.concept(member);
            model.add(collectionURI, SKOS.MEMBER, memberIRI, iriFactory.conceptGraph());
        });

        repositoryGestion.loadSimpleObject(collectionURI, model);

        return collection.getId();
    }
}
