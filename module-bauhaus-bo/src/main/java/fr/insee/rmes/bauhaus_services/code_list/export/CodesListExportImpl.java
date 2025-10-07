package fr.insee.rmes.bauhaus_services.code_list.export;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import fr.insee.rmes.utils.Deserializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CodesListExportImpl implements  CodesListExport {

    private final RepositoryGestion repositoryGestion;

    public CodesListExportImpl(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    @Override
    public ExportedCodesList exportCodesList(String notation) throws RmesException {
        JSONObject codeList = getCodesList(notation);
        codeList.put("codes", getCodes(notation));
        return Deserializer.deserializeJSONObject(codeList, ExportedCodesList.class);
    }

    private JSONObject getCodesList(String notation) throws RmesException {
        JSONObject codeList = this.repositoryGestion.getResponseAsObject(CodeListQueries.getCodeListLabelByNotation(notation));
        return codeList.put(Constants.NOTATION, notation);
    }

    private JSONArray getCodes(String notation) throws RmesException {
        return this.repositoryGestion.getResponseAsArray(CodeListQueries.getCodeListItemsByNotation(notation, 1, 0));
    }
}
