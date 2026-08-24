package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.classifications.nomenclatures.model.Classification;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.XKOS;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.json.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository()
public class ClassificationRepository extends RdfService {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationRepository.class);

    private final BauhausLanguagesProperties languages;


    private final ClassificationNoteService classificationNoteService;
    private final ClassificationsQueries classificationsQueries;
    private final GraphsProperties graphs;

    public ClassificationRepository(RepositoryGestion repoGestion, IdGenerator idGenerator,
                                    RepositoryPublication repositoryPublication, BauhausLanguagesProperties languages,
                                    PublicationUtils publicationUtils,
                                    ClassificationNoteService classificationNoteService,
                                    ClassificationsQueries classificationsQueries,
                                    GraphsProperties graphs) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.languages = languages;
        this.classificationNoteService = classificationNoteService;
        this.classificationsQueries = classificationsQueries;
        this.graphs = graphs;
    }

    public void updateClassification(Classification classification, String uri) throws RmesException {
        Model model = new LinkedHashModel();

        logger.debug("updateClassification - id={}, uri={}", classification.getId(), uri);
        logger.debug("updateClassification - values converted to IRI: creator={}, contributor={}, idSeries={}, additionalMaterial={}, legalMaterial={}, homepage={}, idBefore={}, idAfter={}, idVariant={}",
                classification.getCreator(), classification.getContributor(), classification.getIdSeries(),
                classification.getAdditionalMaterial(), classification.getLegalMaterial(), classification.getHomepage(),
                classification.getIdBefore(), classification.getIdAfter(), classification.getIdVariant());

        this.validate(classification);

        Resource graph = RdfUtils.codesListGraph(classification.getId());
        IRI classificationIri = RdfUtils.createIRI(uri);
        logger.debug("updateClassification - graph={}, classificationIri={}", graph, classificationIri);

        repoGestion.deleteTripletByPredicate(classificationIri, SKOS.PREF_LABEL, graph, null);

        model.add(classificationIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(classification.getPrefLabelLg1(), languages.lg1()), graph);
        model.add(classificationIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(classification.getPrefLabelLg2(), languages.lg2()), graph);

        repoGestion.deleteTripletByPredicate(classificationIri, SKOS.ALT_LABEL, graph, null);

        if(classification.getAltLabelLg1() != null){
            model.add(classificationIri, SKOS.ALT_LABEL, RdfUtils.setLiteralString(classification.getAltLabelLg1(), languages.lg1()), graph);
        }

        if(classification.getAltLabelLg2() != null){
            model.add(classificationIri, SKOS.ALT_LABEL, RdfUtils.setLiteralString(classification.getAltLabelLg2(), languages.lg2()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, DC.DESCRIPTION, graph, null);

        if(classification.getDescriptionLg1() != null){
            model.add(classificationIri, DC.DESCRIPTION, RdfUtils.setLiteralString(classification.getDescriptionLg1(), languages.lg1()), graph);
        }

        if(classification.getDescriptionLg2() != null){
            model.add(classificationIri, DC.DESCRIPTION, RdfUtils.setLiteralString(classification.getDescriptionLg2(), languages.lg2()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.BELONGS_TO, graph, null);
        if(classification.getIdSeries() != null){
            logger.debug("updateClassification - xkos:belongsTo, idSeries={}", classification.getIdSeries());
            model.add(classificationIri, XKOS.BELONGS_TO, RdfUtils.classificationSerieIRI(classification.getIdSeries()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, DC.CREATOR, graph, null);
        logger.debug("updateClassification - dc:creator, raw value expected to be an absolute IRI = [{}]", classification.getCreator());
        RdfUtils.addTripleUri(classificationIri, DC.CREATOR, classification.getCreator(), model, graph);

        repoGestion.deleteTripletByPredicate(classificationIri, DC.CONTRIBUTOR, graph, null);
        logger.debug("updateClassification - dc:contributor, raw value expected to be an absolute IRI = [{}]", classification.getContributor());
        RdfUtils.addTripleUri(classificationIri, DC.CONTRIBUTOR, classification.getContributor(), model, graph);

        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.ADDITIONALMATERIAL, graph, null);
        if(StringUtils.isNotEmpty(classification.getAdditionalMaterial())){
            logger.debug("updateClassification - insee:additionalMaterial = [{}]", classification.getAdditionalMaterial());
            model.add(classificationIri, INSEE.ADDITIONALMATERIAL, RdfUtils.createIRI(classification.getAdditionalMaterial()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.LEGALMATERIAL, graph, null);
        if(StringUtils.isNotEmpty(classification.getLegalMaterial())){
            logger.debug("updateClassification - insee:legalMaterial = [{}]", classification.getLegalMaterial());
            model.add(classificationIri, INSEE.LEGALMATERIAL, RdfUtils.createIRI(classification.getLegalMaterial()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.DISSEMINATIONSTATUS, graph, null);
        if(classification.getDisseminationStatus() != null){
            model.add(classificationIri, INSEE.DISSEMINATIONSTATUS, RdfUtils.setLiteralString(classification.getDisseminationStatus()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, FOAF.HOMEPAGE, graph, null);
        if(StringUtils.isNotEmpty(classification.getHomepage())){
            logger.debug("updateClassification - foaf:homepage = [{}]", classification.getHomepage());
            model.add(classificationIri, FOAF.HOMEPAGE, RdfUtils.createIRI(classification.getHomepage()), graph);
        }

        // Le statut de validation est porté par le graphe des nomenclatures (= classifFamiliesGraph),
        // là où le schéma est enregistré et où la publication l'écrit. On l'écrit donc dans ce graphe
        // (et pas dans le graphe par-id) pour que lecture, modification et publication restent cohérentes.
        logger.debug("updateClassification - validationGraph (classifFamiliesGraph) = [{}]", graphs.classifFamiliesGraph());
        Resource validationGraph = RdfUtils.createIRI(graphs.classifFamiliesGraph());
        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.VALIDATION_STATE, graph, null);
        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.VALIDATION_STATE, validationGraph, null);

        ValidationStatus newValidationState =
            ValidationStatus.VALIDATED.getValue().equalsIgnoreCase(classification.getValidationState())
                || ValidationStatus.MODIFIED.getValue().equalsIgnoreCase(classification.getValidationState())
                ? ValidationStatus.MODIFIED
                : ValidationStatus.UNPUBLISHED;
        model.add(classificationIri, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(newValidationState), validationGraph);


        List<String> ids = new ArrayList<>();
        if(classification.getIdBefore() != null){
            ids.add(classification.getIdBefore());
        }
        if(classification.getIdAfter() != null){
            ids.add(classification.getIdAfter());
        }
        if(classification.getIdVariant() != null){
            ids.add(classification.getIdVariant());
        }
        String[] idsArray = new String[ids.size()];

        for (int i = 0; i < ids.size(); i++) {
            idsArray[i] = ids.get(i);
        }
        JSONArray codes = repoGestion.getResponseAsArray(classificationsQueries.classificationsUriById(idsArray));
        logger.debug("updateClassification - before/after/variant ids={}, resolved uris={}", ids, codes);

        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.BEFORE, graph, null);
        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.AFTER, graph, null);
        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.VARIANT, graph, null);

        JSONUtils.stream(codes).forEach(code -> {
            String codeUri = code.getString("uri");
            String codeId = code.getString("id");
            logger.debug("updateClassification - xkos link, codeId={}, codeUri=[{}]", codeId, codeUri);

            if(codeId.equalsIgnoreCase(classification.getIdBefore())){
                model.add(classificationIri, XKOS.BEFORE, RdfUtils.createIRI(codeUri), graph);
            }
            if(codeId.equalsIgnoreCase(classification.getIdAfter())){
                model.add(classificationIri, XKOS.AFTER, RdfUtils.createIRI(codeUri), graph);
            }
            if(codeId.equalsIgnoreCase(classification.getIdVariant())){
                model.add(classificationIri, XKOS.VARIANT, RdfUtils.createIRI(codeUri), graph);
            }
        });

        this.classificationNoteService.addNotes(graph, classification.getChangeNoteUriLg1(), classification.getChangeNoteLg1(), model);
        this.classificationNoteService.addNotes(graph, classification.getChangeNoteUriLg2(), classification.getChangeNoteUriLg2(), model);
        this.classificationNoteService.addNotes(graph, classification.getScopeNoteUriLg1(), classification.getScopeNoteLg1(), model);
        this.classificationNoteService.addNotes(graph, classification.getScopeNoteUriLg2(), classification.getScopeNoteLg2(), model);
        logger.debug("updateClassification - model ready ({} triples) for {}", model.size(), classificationIri);
        repoGestion.loadSimpleObjectWithoutDeletion(classificationIri, model, null);
    }

    private void validate(Classification classification) throws RmesBadRequestException {
        if(classification.getPrefLabelLg1() == null){
            throw new RmesBadRequestException("The property prefLabelLg1 is required");
        }
    }
}
