package fr.insee.rmes.bauhaus_services.classifications.item;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.classification.ClassificationItem;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.XKOS;
import fr.insee.rmes.persistance.sparql_queries.classifications.ItemsQueries;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.SKOSXL;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import static fr.insee.rmes.utils.StringUtils.convertHtmlStringToRaw;

@Repository()
public class ClassificationItemUtils extends RdfService {
    public void updateClassificationItem(ClassificationItem item, String itemUri, String classificationId) throws RmesException {
        JSONObject previousItem = repoGestion.getResponseAsObject(ItemsQueries.itemQuery(classificationId, item.getId()));

        Model model = new LinkedHashModel();
        this.validate(item);
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



        addNote(item.getDefinitionLg1Uri(), item.getDefinitionLg1(), graph, model);
        addNote(item.getDefinitionLg2Uri(), item.getDefinitionLg2(), graph, model);

        addNote(item.getScopeNoteLg1Uri(), item.getScopeNoteLg1(), graph, model);
        addNote(item.getScopeNoteLg2Uri(), item.getScopeNoteLg2(), graph, model);

        addNote(item.getCoreContentNoteLg1Uri(), item.getCoreContentNoteLg1(), graph, model);
        addNote(item.getCoreContentNoteLg2Uri(), item.getCoreContentNoteLg2(), graph, model);

        addNote(item.getAdditionalContentNoteLg1Uri(), item.getAdditionalContentNoteLg1(), graph, model);
        addNote(item.getAdditionalContentNoteLg2Uri(), item.getAdditionalContentNoteLg2(), graph, model);

        addNote(item.getExclusionNoteLg1Uri(), item.getExclusionNoteLg1(), graph, model);
        addNote(item.getExclusionNoteLg2Uri(), item.getExclusionNoteLg2(), graph, model);

        addNote(item.getChangeNoteLg1Uri(), item.getChangeNoteLg1(), graph, model);
        addNote(item.getChangeNoteLg2Uri(), item.getChangeNoteLg2(), graph, model);

        repoGestion.loadSimpleObjectWithoutDeletion(classificationItemIri, model, null);
    }

    private void addNote(String iri, String value, Resource graph, Model model) throws RmesException {
        if(iri != null){
            IRI finalIri = RdfUtils.createIRI(iri);
            repoGestion.deleteTripletByPredicate(finalIri, EVOC.NOTE_LITERAL, graph, null);
            repoGestion.deleteTripletByPredicate(finalIri, XKOS.PLAIN_TEXT, graph, null);

            String html = XhtmlToMarkdownUtils.markdownToXhtml(value);
            String raw = convertHtmlStringToRaw(html);
            model.add(finalIri, EVOC.NOTE_LITERAL, RdfUtils.setLiteralString(html), graph);
            model.add(finalIri, XKOS.PLAIN_TEXT, RdfUtils.setLiteralString(raw), graph);
            model.add(finalIri, RDF.VALUE, RdfUtils.setLiteralString(value), graph);
        }
    }

    private void validate(ClassificationItem item) throws RmesBadRequestException {
        if(item.getPrefLabelLg1() == null){
            throw new RmesBadRequestException("The property prefLabelLg1 is required");
        }
    }
}
