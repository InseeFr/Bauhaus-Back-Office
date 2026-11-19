package fr.insee.rmes.modules.operations.msd.domain.model;

public record DocumentationAttribute(
        String rangeType,
        String masLabelLg1,
        String masLabelLg2,
        String id,
        String maxOccurs,
        Boolean isPresentational,
        Boolean sansObject,
        String codeList,
        String subPropertyOf,
        String instructionLg1,
        String instructionLg2) {}
