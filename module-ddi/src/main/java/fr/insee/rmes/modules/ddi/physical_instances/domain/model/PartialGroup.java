package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import java.util.Date;

public record PartialGroup(String id, String label, Date versionDate, String agency) {
}