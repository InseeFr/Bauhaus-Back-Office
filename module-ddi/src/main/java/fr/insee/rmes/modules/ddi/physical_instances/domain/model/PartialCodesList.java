package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.Date;

public record PartialCodesList(String id, String label, Date versionDate, String agency, String name) {

    /**
     * Variante sans nom technique : {@code name} vaut {@code null}. Conserve la compatibilité
     * avec les appelants qui ne disposent que du libellé (listes du groupe, tests existants).
     */
    public PartialCodesList(String id, String label, Date versionDate, String agency) {
        this(id, label, versionDate, agency, null);
    }
}
