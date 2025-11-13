package fr.insee.rmes.bauhaus_services.code_list.export;

import fr.insee.rmes.Constants;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.codeslists.infrastructure.graphdb.CodeListsQueries;
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
        JSONObject codeList = this.repositoryGestion.getResponseAsObject(CodeListsQueries.getCodeListLabelByNotation(notation));
        return codeList.put(Constants.NOTATION, notation);
    }

    private JSONArray getCodes(String notation) throws RmesException {
        return this.repositoryGestion.getResponseAsArray(CodeListsQueries.getCodeListItemsByNotation(notation, 1, 0));
    }
}
