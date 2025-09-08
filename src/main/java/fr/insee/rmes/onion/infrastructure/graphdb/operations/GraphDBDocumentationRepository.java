package fr.insee.rmes.onion.infrastructure.graphdb.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.model.operations.documentations.RangeType;
import fr.insee.rmes.onion.domain.exceptions.GenericInternalServerException;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.onion.domain.exceptions.operations.NotFoundAttributeException;
import fr.insee.rmes.onion.domain.exceptions.operations.OperationDocumentationRubricWithoutRangeException;
import fr.insee.rmes.onion.domain.model.operations.DocumentationAttribute;
import fr.insee.rmes.onion.domain.port.serverside.DocumentationRepository;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.DocumentationQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GraphDBDocumentationRepository implements DocumentationRepository {

    private static final String RANGE = "range";

    private final RepositoryGestion repositoryGestion;

    public GraphDBDocumentationRepository(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public List<DocumentationAttribute> getAttributesSpecification() throws GenericInternalServerException, OperationDocumentationRubricWithoutRangeException {
        JSONArray attributesList;
        try {
            attributesList = repositoryGestion.getResponseAsArray(DocumentationQueries.getAttributesQuery());
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }

        List<DocumentationAttribute> documentationAttributes = new ArrayList<>();

        if (!attributesList.isEmpty()) {
            for (int i = 0; i < attributesList.length(); i++) {
                JSONObject attribute = attributesList.getJSONObject(i);
                transformRangeType(attribute);
                documentationAttributes.add(DocumentationAttribute.fromJson(attribute));
            }
        }
        return documentationAttributes;
    }

    @Override
    public DocumentationAttribute getAttributeSpecification(String id) throws GenericInternalServerException, OperationDocumentationRubricWithoutRangeException, NotFoundAttributeException {
        JSONObject mas;
        try {
            mas = repositoryGestion.getResponseAsObject(DocumentationQueries.getAttributeSpecificationQuery(id));
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }
        if (mas.isEmpty()) {
            throw new NotFoundAttributeException(id);
        }
        transformRangeType(mas);
        mas.put(Constants.ID, id);
        return DocumentationAttribute.fromJson(mas);
    }

    private void transformRangeType(JSONObject mas) throws OperationDocumentationRubricWithoutRangeException {
        if (!mas.has(RANGE)) {
            throw new OperationDocumentationRubricWithoutRangeException((mas.has("id") ? mas.get(Constants.ID) : mas).toString());
        }
        String rangeUri = mas.getString(RANGE);
        RangeType type = RangeType.getEnumByRdfType(RdfUtils.toURI(rangeUri));
        mas.put(Constants.RANGE_TYPE, type.getJsonType());
        mas.remove(RANGE);

        if (!type.equals(RangeType.CODELIST)) {
            mas.remove(Constants.CODELIST);
        }

    }
}
