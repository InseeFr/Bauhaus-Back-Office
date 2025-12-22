package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.modules.commons.domain.model.DisseminationStatus;

public record DisseminationStatusDTO(String label, String url) {
    public static DisseminationStatusDTO fromDomain(DisseminationStatus status) {
        return new DisseminationStatusDTO(status.getLabel(), status.getUrl());
    }
}