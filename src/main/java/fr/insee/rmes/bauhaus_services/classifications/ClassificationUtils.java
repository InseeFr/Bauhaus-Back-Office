package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.classification.Classification;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.XKOS;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository()
public class ClassificationUtils extends RdfService {
    public void updateClassification(Classification classification, String uri) throws RmesException {
        Model model = new LinkedHashModel();

        this.validate(classification);
        
        Resource graph = RdfUtils.codesListGraph(classification.getId());
        IRI classificationIri = RdfUtils.createIRI(uri);

        repoGestion.deleteTripletByPredicate(classificationIri, SKOS.PREF_LABEL, graph, null);

        model.add(classificationIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(classification.getPrefLabelLg1(), config.getLg1()), graph);
        model.add(classificationIri, SKOS.PREF_LABEL, RdfUtils.setLiteralString(classification.getPrefLabelLg2(), config.getLg2()), graph);

        repoGestion.deleteTripletByPredicate(classificationIri, SKOS.ALT_LABEL, graph, null);

        if(classification.getAltLabelLg1() != null){
            model.add(classificationIri, SKOS.ALT_LABEL, RdfUtils.setLiteralString(classification.getAltLabelLg1(), config.getLg1()), graph);
        }

        if(classification.getAltLabelLg2() != null){
            model.add(classificationIri, SKOS.ALT_LABEL, RdfUtils.setLiteralString(classification.getAltLabelLg2(), config.getLg2()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, DC.DESCRIPTION, graph, null);

        if(classification.getDescriptionLg1() != null){
            model.add(classificationIri, DC.DESCRIPTION, RdfUtils.setLiteralString(XhtmlToMarkdownUtils.markdownToXhtml(classification.getDescriptionLg1()), config.getLg1()), graph);
        }

        if(classification.getDescriptionLg2() != null){
            model.add(classificationIri, DC.DESCRIPTION, RdfUtils.setLiteralString(XhtmlToMarkdownUtils.markdownToXhtml(classification.getDescriptionLg2()), config.getLg2()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.BELONGS_TO, graph, null);
        if(classification.getIdSeries() != null){
            model.add(classificationIri, XKOS.BELONGS_TO, RdfUtils.classificationSerieIRI(classification.getIdSeries()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, DC.CREATOR, graph, null);
        if(classification.getCreator() != null){
            //TODO FRONT
            model.add(classificationIri, DC.CREATOR, RdfUtils.setLiteralString(classification.getCreator()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, DC.CONTRIBUTOR, graph, null);
        if(classification.getContributor() != null){
            //TODO FRONT
            model.add(classificationIri, DC.CONTRIBUTOR, RdfUtils.setLiteralString(classification.getContributor()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.ADDITIONALMATERIAL, graph, null);
        if(StringUtils.isNotEmpty(classification.getAdditionalMaterial())){
            model.add(classificationIri, INSEE.ADDITIONALMATERIAL, RdfUtils.createIRI(classification.getAdditionalMaterial()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.LEGALMATERIAL, graph, null);
        if(StringUtils.isNotEmpty(classification.getLegalMaterial())){
            model.add(classificationIri, INSEE.LEGALMATERIAL, RdfUtils.createIRI(classification.getLegalMaterial()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, INSEE.DISSEMINATIONSTATUS, graph, null);
        if(classification.getDisseminationStatus() != null){
            model.add(classificationIri, INSEE.DISSEMINATIONSTATUS, RdfUtils.setLiteralString(classification.getDisseminationStatus()), graph);
        }

        repoGestion.deleteTripletByPredicate(classificationIri, FOAF.HOMEPAGE, graph, null);
        if(StringUtils.isNotEmpty(classification.getHomepage())){
            model.add(classificationIri, FOAF.HOMEPAGE, RdfUtils.createIRI(classification.getHomepage()), graph);
        }


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
        JSONArray codes = repoGestion.getResponseAsArray(ClassificationsQueries.classificationsUriById(idsArray));

        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.BEFORE, graph, null);
        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.AFTER, graph, null);
        repoGestion.deleteTripletByPredicate(classificationIri, XKOS.VARIANT, graph, null);

        for (int i=0; i<codes.length(); i++) {
            JSONObject code = codes.getJSONObject(i);
            String codeUri = code.getString("uri");
            String codeId = code.getString("id");

            if(codeId.equalsIgnoreCase(classification.getIdBefore())){
                model.add(classificationIri, XKOS.BEFORE, RdfUtils.createIRI(codeUri), graph);
            }
            if(codeId.equalsIgnoreCase(classification.getIdAfter())){
                model.add(classificationIri, XKOS.AFTER, RdfUtils.createIRI(codeUri), graph);
            }
            if(codeId.equalsIgnoreCase(classification.getIdVariant())){
                model.add(classificationIri, XKOS.VARIANT, RdfUtils.createIRI(codeUri), graph);
            }
        }

        saveClassificationNote(graph, classification.getChangeNoteUriLg1(), classification.getChangeNoteLg1(), model);
        saveClassificationNote(graph, classification.getChangeNoteUriLg2(), classification.getChangeNoteUriLg2(), model);
        saveClassificationNote(graph, classification.getScopeNoteUriLg1(), classification.getScopeNoteLg1(), model);
        saveClassificationNote(graph, classification.getScopeNoteUriLg2(), classification.getScopeNoteLg2(), model);
        repoGestion.loadSimpleObjectWithoutDeletion(classificationIri, model, null);
    }

    private void saveClassificationNote(Resource graph, String noteUri, String noteValue, Model model) throws RmesException {
        if (StringUtils.isNotEmpty(noteUri)) {
            IRI noteIri = RdfUtils.createIRI(noteUri);
            repoGestion.deleteTripletByPredicate(noteIri, EVOC.NOTE_LITERAL, graph, null);
            repoGestion.deleteTripletByPredicate(noteIri, XKOS.PLAIN_TEXT, graph, null);
            if (StringUtils.isNotEmpty(noteValue)) {
                String html = XhtmlToMarkdownUtils.markdownToXhtml(noteValue);
                String raw = html.replaceAll("<[^>]*>", "");
                model.add(noteIri, EVOC.NOTE_LITERAL, RdfUtils.setLiteralString(html), graph);
                model.add(noteIri, XKOS.PLAIN_TEXT, RdfUtils.setLiteralString(raw), graph);
            }
        }
    }

    private void validate(Classification classification) throws RmesBadRequestException {
        if(classification.getPrefLabelLg1() == null){
            throw new RmesBadRequestException("The property prefLabelLg1 is required");
        }
    }
}
