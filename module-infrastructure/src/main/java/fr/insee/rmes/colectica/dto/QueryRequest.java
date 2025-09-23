package fr.insee.rmes.colectica.dto;

import java.util.List;

public record QueryRequest(
    List<String> itemTypes
) {}