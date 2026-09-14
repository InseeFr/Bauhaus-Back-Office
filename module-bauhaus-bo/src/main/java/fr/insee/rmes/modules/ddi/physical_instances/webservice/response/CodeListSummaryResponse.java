package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import java.util.Date;

public record CodeListSummaryResponse(String agencyId, String id, String label, String name, Date versionDate) {

    /**
     * Variante sans date de version : {@code versionDate} vaut {@code null}.
     */
    public CodeListSummaryResponse(String agencyId, String id, String label, String name) {
        this(agencyId, id, label, name, null);
    }

    /**
     * Variante sans nom technique ni date : {@code name} et {@code versionDate} valent {@code null}.
     * Utilisée pour les listes qui ne portent que le libellé (cf. {@link #fromDdi4CodeList(Ddi4CodeList)}).
     */
    public CodeListSummaryResponse(String agencyId, String id, String label) {
        this(agencyId, id, label, null, null);
    }

    public static CodeListSummaryResponse fromDdi4CodeList(Ddi4CodeList codeList) {
        String labelText = null;
        if (codeList.label() != null && !codeList.label().isEmpty()) {
            labelText = codeList.label().get(0).value();
        }
        return new CodeListSummaryResponse(codeList.agency(), codeList.id(), labelText);
    }
}
