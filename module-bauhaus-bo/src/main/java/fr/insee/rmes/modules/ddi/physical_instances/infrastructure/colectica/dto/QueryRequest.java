package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import java.util.List;

public record QueryRequest(
    List<String> itemTypes
) {}