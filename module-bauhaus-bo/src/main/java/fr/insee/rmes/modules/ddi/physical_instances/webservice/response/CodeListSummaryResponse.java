package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;

public record CodeListSummaryResponse(String agencyId, String id, String label) {
    public static CodeListSummaryResponse fromDdi4CodeList(Ddi4CodeList codeList) {
        String labelText = null;
        if (codeList.label() != null && !codeList.label().isEmpty()) {
            labelText = codeList.label().get(0).value();
        }
        return new CodeListSummaryResponse(codeList.agency(), codeList.id(), labelText);
    }
}
