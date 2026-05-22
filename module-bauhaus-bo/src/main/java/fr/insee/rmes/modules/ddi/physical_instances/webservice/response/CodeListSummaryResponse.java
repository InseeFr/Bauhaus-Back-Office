package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.generated.CodeList;

public record CodeListSummaryResponse(String agencyId, String id, String label) {
    public static CodeListSummaryResponse fromDdi4CodeList(CodeList codeList) {
        String labelText = null;
        if (codeList.getLabel() != null && !codeList.getLabel().isEmpty()) {
            labelText = codeList.getLabel().get(0).getAtValue();
        }
        return new CodeListSummaryResponse(codeList.getAgency(), codeList.getID(), labelText);
    }
}
