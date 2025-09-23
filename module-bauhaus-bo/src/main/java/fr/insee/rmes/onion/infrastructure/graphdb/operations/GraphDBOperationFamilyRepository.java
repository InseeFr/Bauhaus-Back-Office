package fr.insee.rmes.onion.infrastructure.graphdb.operations;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.operations.families.OperationFamily;
import fr.insee.rmes.domain.model.operations.families.OperationFamilySeries;
import fr.insee.rmes.domain.model.operations.families.OperationFamilySubject;
import fr.insee.rmes.domain.model.operations.families.PartialOperationFamily;
import fr.insee.rmes.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.OperationFamilyQueries;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GraphDBOperationFamilyRepository implements OperationFamilyRepository {

    private final RepositoryGestion repositoryGestion;
    private final OperationFamilyQueries operationFamilyQueries;
    private final boolean familiesRichTextNexStructure;

    public GraphDBOperationFamilyRepository(
            RepositoryGestion repositoryGestion,
            OperationFamilyQueries operationFamilyQueries,
            @Value("${fr.insee.rmes.bauhaus.feature-flipping.operations.families-rich-text-new-structure}") boolean familiesRichTextNexStructure
    ) {
        this.repositoryGestion = repositoryGestion;
        this.operationFamilyQueries = operationFamilyQueries;
        this.familiesRichTextNexStructure = familiesRichTextNexStructure;
    }

    @Override
    public List<PartialOperationFamily> getFamilies() throws RmesException {
        var families = this.repositoryGestion.getResponseAsArray(operationFamilyQueries.familiesQuery());

        return DiacriticSorter.sort(families,
                PartialOperationFamily[].class,
                PartialOperationFamily::label);
    }


    @Override
    public OperationFamily getFullFamily(String id) throws RmesException {
        var family = getFamily(id);
        var series = getFamilySeries(id);
        if(!series.isEmpty()){
            family = family.withSeries(series);
        }

        var subjects = getFamilySubjects(id);
        if(!subjects.isEmpty()){
            family = family.withSubject(subjects);
        }

        return family;
    }

    @Override
    public OperationFamily getFamily(String id) throws RmesException {
        var family = this.repositoryGestion.getResponseAsObject(operationFamilyQueries.familyQuery(id, this.familiesRichTextNexStructure));

        if (family.isEmpty()) {
            throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Family " + id + " not found", "Maybe id is wrong");
        }
        XhtmlToMarkdownUtils.convertJSONObject(family);

        return OperationFamily.fromJson(family);
    }

    @Override
    public List<OperationFamilySeries> getFamilySeries(String id) throws RmesException {
        var array = repositoryGestion.getResponseAsArray(operationFamilyQueries.getSeries(id));
        List<OperationFamilySeries> series = new ArrayList<>();

        if (!array.isEmpty()) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject attribute = array.getJSONObject(i);
                series.add(OperationFamilySeries.fromJSON(attribute));
            }
        }
        return series;
    }

    @Override
    public List<OperationFamilySubject> getFamilySubjects(String id) throws RmesException {
        var array = repositoryGestion.getResponseAsArray(operationFamilyQueries.getSubjects(id));
        List<OperationFamilySubject> subjects = new ArrayList<>();

        if (!array.isEmpty()) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject attribute = array.getJSONObject(i);
                subjects.add(OperationFamilySubject.fromJSON(attribute));
            }
        }
        return subjects;
    }
}


