package fr.insee.rmes.bauhaus_services.classifications.item;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.classification.ClassificationItem;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.stereotype.Repository;

@Repository()
public class ClassificationItemUtils extends RdfService {
    public void updateClassificationItem(ClassificationItem item, String itemUri, String classificationId) throws RmesException {
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
            model.add(classificationItemIri, SKOS.BROADER, RdfUtils.createIRI(item.getBroaderURI()), graph);
        }
        repoGestion.loadSimpleObjectWithoutDeletion(classificationItemIri, model, null);
    }

    private void validate(ClassificationItem item) throws RmesBadRequestException {
        if(item.getPrefLabelLg1() == null){
            throw new RmesBadRequestException("The property prefLabelLg1 is required");
        }
    }
}
