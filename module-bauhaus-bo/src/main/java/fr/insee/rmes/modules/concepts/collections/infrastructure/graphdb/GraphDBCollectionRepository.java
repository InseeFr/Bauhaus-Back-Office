package fr.insee.rmes.modules.concepts.collections.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionRepository;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class GraphDBCollectionRepository implements CollectionRepository  {

    private final RepositoryGestion repositoryGestion;
    private final BauhausLanguagesProperties languages;

    public GraphDBCollectionRepository(
            RepositoryGestion repositoryGestion,
            BauhausLanguagesProperties languages
    ) {
        this.repositoryGestion = repositoryGestion;
        this.languages = languages;
    }

    @Override
    public String save(Collection collection) throws RmesException {
        Model model = new LinkedHashModel();
        IRI collectionURI = RdfUtils.collectionIRI(collection.getId());
        model.add(collectionURI, RDF.TYPE, SKOS.COLLECTION, RdfUtils.conceptGraph());
        model.add(collectionURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(Boolean.TRUE.equals(collection.getIsValidated()) ? ValidationStatus.VALIDATED : ValidationStatus.UNPUBLISHED), RdfUtils.conceptGraph());
        model.add(collectionURI, DCTERMS.TITLE, RdfUtils.setLiteralString(collection.getPrefLabelLg1(), languages.lg1()), RdfUtils.conceptGraph());
        model.add(collectionURI, DCTERMS.CREATED, RdfUtils.setLiteralDateTime(collection.getCreated()), RdfUtils.conceptGraph());
        RdfUtils.addTripleDate(collectionURI, DCTERMS.MODIFIED, LocalDateTime.now().toString(), model, RdfUtils.conceptGraph());
        model.add(collectionURI, DC.CONTRIBUTOR, RdfUtils.setLiteralString(collection.getContributor()), RdfUtils.conceptGraph());
        model.add(collectionURI, DC.CREATOR, RdfUtils.setLiteralString(collection.getCreator()), RdfUtils.conceptGraph());
        /*Optional*/
        RdfUtils.addTripleDateTime(collectionURI, DCTERMS.MODIFIED, collection.getModified(), model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.TITLE, collection.getPrefLabelLg2(), languages.lg2(), model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg1(), languages.lg1(), model, RdfUtils.conceptGraph());
        RdfUtils.addTripleString(collectionURI, DCTERMS.DESCRIPTION, collection.getDescriptionLg2(), languages.lg2(), model, RdfUtils.conceptGraph());

        /*Members*/
        collection.getMembers().forEach(member->{
            IRI memberIRI = RdfUtils.conceptIRI(member);
            model.add(collectionURI, SKOS.MEMBER, memberIRI, RdfUtils.conceptGraph());
        });

        repositoryGestion.loadSimpleObject(collectionURI, model);

        return collection.getId();
    }
}
