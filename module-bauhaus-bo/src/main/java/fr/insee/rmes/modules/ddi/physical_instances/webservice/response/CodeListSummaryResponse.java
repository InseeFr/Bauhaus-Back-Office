package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;

public record CodeListSummaryResponse(String agencyId, String id, String label, String name) {

    /**
     * Variante sans nom technique : {@code name} vaut {@code null}. Utilisée pour les listes
     * qui ne portent que le libellé (cf. {@link #fromDdi4CodeList(Ddi4CodeList)}).
     */
    public CodeListSummaryResponse(String agencyId, String id, String label) {
        this(agencyId, id, label, null);
    }

    public static CodeListSummaryResponse fromDdi4CodeList(Ddi4CodeList codeList) {
        String labelText = null;
        if (codeList.label() != null && !codeList.label().isEmpty()) {
            labelText = codeList.label().get(0).value();
        }
        return new CodeListSummaryResponse(codeList.agency(), codeList.id(), labelText);
    }
}
