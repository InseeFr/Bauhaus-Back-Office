package fr.insee.rmes.bauhaus_services.classifications.item;

import fr.insee.rmes.bauhaus_services.classifications.ClassificationNoteService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.classification.ClassificationItem;
import fr.insee.rmes.persistance.sparql_queries.classifications.ItemsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.SKOSXL;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository()
public class ClassificationItemRepository {
    ClassificationNoteService classificationNoteService;
    RepositoryGestion repoGestion;
    Config config;

    public ClassificationItemRepository(ClassificationNoteService classificationNoteService, RepositoryGestion repoGestion, Config config) {
        this.classificationNoteService = classificationNoteService;
        this.repoGestion = repoGestion;
        this.config = config;
    }

    public void updateClassificationItem(ClassificationItem item, String itemUri, String classificationId) throws RmesException {
        this.validate(item);

        JSONObject previousItem = repoGestion.getResponseAsObject(ItemsQueries.itemQuery(classificationId, item.getId()));

        Model model = new LinkedHashModel();

        Resource graph = RdfUtils.codesListGraph(classificationId);

        IRI classificationItemIri = RdfUtils.createIRI(itemUri);

        repoGestion.deleteTripletByPredicate(classificationItemIri, SKOS.PREF_LABEL, graph, null);

        model.add(classificationItemIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(item.getPrefLabelLg1(), config.getLg1()), graph);
        model.add(classificationItemIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(item.getPrefLabelLg2(), config.getLg2()), graph);

        repoGestion.deleteTripletByPredicate(classificationItemIri, SKOS.ALT_LABEL, graph, null);

        if(item.getAltLabelLg1() != null){
            model.add(classificationItemIri, SKOS.ALT_LABEL, RdfUtils.setLiteralString(item.getAltLabelLg1(), config.getLg1()), graph);
        }

        if(item.getAltLabelLg2() != null){
            model.add(classificationItemIri, SKOS.ALT_LABEL, RdfUtils.setLiteralString(item.getAltLabelLg2(), config.getLg2()), graph);
        }


        repoGestion.deleteTripletByPredicate(classificationItemIri, SKOS.BROADER, graph, null);
        if(item.getBroaderURI() != null){

            if(previousItem.has("broaderURI")){
                repoGestion.deleteTripletByPredicateAndValue(RdfUtils.createIRI(previousItem.getString("broaderURI")), SKOS.NARROWER, graph, null, classificationItemIri);
            }
            IRI broaderIri = RdfUtils.createIRI(item.getBroaderURI());
            model.add(broaderIri, SKOS.NARROWER, classificationItemIri, graph);
            model.add(classificationItemIri, SKOS.BROADER, broaderIri, graph);
        }

        if(item.getAltLabels() != null) {
            item.getAltLabels().stream().forEach(altLabel -> {
                try {
                    IRI altLabelIri = RdfUtils.createIRI(altLabel.getShortLabelUri());
                    repoGestion.deleteTripletByPredicate(altLabelIri, SKOSXL.LITERAL_FORM, graph, null);
                    if(altLabel.getShortLabelLg1() != null){
                        model.add(altLabelIri, SKOSXL.LITERAL_FORM, RdfUtils.setLiteralString(altLabel.getShortLabelLg1(), config.getLg1()), graph);
                    }
                    if(altLabel.getShortLabelLg2() != null){
                        model.add(altLabelIri, SKOSXL.LITERAL_FORM, RdfUtils.setLiteralString(altLabel.getShortLabelLg2(), config.getLg2()), graph);
                    }
                } catch (RmesException e) {
                    e.printStackTrace();
                }
            });
        }



        this.classificationNoteService.addNotes(graph, item.getDefinitionLg1Uri(), item.getDefinitionLg1(), model);
        this.classificationNoteService.addNotes(graph, item.getDefinitionLg2Uri(), item.getDefinitionLg2(), model);

        this.classificationNoteService.addNotes(graph, item.getScopeNoteLg1Uri(), item.getScopeNoteLg1(), model);
        this.classificationNoteService.addNotes(graph, item.getScopeNoteLg2Uri(), item.getScopeNoteLg2(), model);

        this.classificationNoteService.addNotes(graph, item.getCoreContentNoteLg1Uri(), item.getCoreContentNoteLg1(), model);
        this.classificationNoteService.addNotes(graph, item.getCoreContentNoteLg2Uri(), item.getCoreContentNoteLg2(), model);

        this.classificationNoteService.addNotes(graph, item.getAdditionalContentNoteLg1Uri(), item.getAdditionalContentNoteLg1(), model);
        this.classificationNoteService.addNotes(graph, item.getAdditionalContentNoteLg2Uri(), item.getAdditionalContentNoteLg2(), model);

        this.classificationNoteService.addNotes(graph, item.getExclusionNoteLg1Uri(), item.getExclusionNoteLg1(), model);
        this.classificationNoteService.addNotes(graph, item.getExclusionNoteLg2Uri(), item.getExclusionNoteLg2(), model);

        this.classificationNoteService.addNotes(graph, item.getChangeNoteLg1Uri(), item.getChangeNoteLg1(), model);
        this.classificationNoteService.addNotes(graph, item.getChangeNoteLg2Uri(), item.getChangeNoteLg2(), model);

        repoGestion.loadSimpleObjectWithoutDeletion(classificationItemIri, model, null);
    }


    private void validate(ClassificationItem item) throws RmesBadRequestException {
        if(item.getPrefLabelLg1() == null){
            throw new RmesBadRequestException("The property prefLabelLg1 is required");
        }
        if(item.getPrefLabelLg2() == null){
            throw new RmesBadRequestException("The property prefLabelLg2 is required");
        }
    }
}
