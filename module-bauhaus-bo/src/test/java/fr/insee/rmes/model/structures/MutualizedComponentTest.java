package fr.insee.rmes.model.structures;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.components.domain.model.MutualizedComponent;
import fr.insee.rmes.modules.structures.structures.domain.model.Structure;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MutualizedComponentTest {

    @Test
    void shouldReturnMutualizedComponentAttributes() throws RmesException {

        MutualizedComponent mutualizedComponent = new MutualizedComponent();

        List<String> attributes = List.of("mockedIdentifiant",
                "mockedIdentifiant",
                "mockedLabelLg1",
                "mockedLabelLg2",
                "mockedAltLabelLg1",
                "mockedAltLabelLg2",
                "mockedDescriptionLg1",
                "mockedDescriptionLg2",
                "mockedType",
                "mockedConcept",
                "mockedCodeList",
                "mockedFullCodeListValue",
                "mockedRange",
                "mockedCreated",
                "mockedUpdated",
                "mockedCreator",
                "mockedDisseminationStatus",
                "mockedMinLength",
                "mockedMaxLength",
                "mockedMinInclusive",
                "mockedMaxInclusive",
                "mockedPattern"
                );

        mutualizedComponent.setIdentifiant(attributes.getFirst());
        mutualizedComponent.setId(attributes.get(1));
        mutualizedComponent.setLabelLg1(attributes.get(2));
        mutualizedComponent.setLabelLg2(attributes.get(3));
        mutualizedComponent.setAltLabelLg1(attributes.get(4));
        mutualizedComponent.setAltLabelLg2(attributes.get(5));
        mutualizedComponent.setDescriptionLg1(attributes.get(6));
        mutualizedComponent.setDescriptionLg2(attributes.get(7));
        mutualizedComponent.setType(attributes.get(8));
        mutualizedComponent.setConcept(attributes.get(9));
        mutualizedComponent.setCodeList(attributes.get(10));
        mutualizedComponent.setFullCodeListValue(attributes.get(11));
        mutualizedComponent.setRange(attributes.get(12));
        mutualizedComponent.setCreated(attributes.get(13));
        mutualizedComponent.setUpdated(attributes.get(14));
        mutualizedComponent.setCreator(attributes.get(15));
        mutualizedComponent.setDisseminationStatus(attributes.get(16));
        mutualizedComponent.setMinLength(attributes.get(17));
        mutualizedComponent.setMaxLength(attributes.get(18));
        mutualizedComponent.setMinInclusive(attributes.get(19));
        mutualizedComponent.setMaxInclusive(attributes.get(20));
        mutualizedComponent.setPattern(attributes.get(21));

        List<String> actualAttributes= List.of(
                mutualizedComponent.getIdentifiant(),
                mutualizedComponent.getId(),
                mutualizedComponent.getLabelLg1(),
                mutualizedComponent.getLabelLg2(),
                mutualizedComponent.getAltLabelLg1(),
                mutualizedComponent.getAltLabelLg2(),
                mutualizedComponent.getDescriptionLg1(),
                mutualizedComponent.getDescriptionLg2(),
                mutualizedComponent.getType(),
                mutualizedComponent.getConcept(),
                mutualizedComponent.getCodeList(),
                mutualizedComponent.getFullCodeListValue(),
                mutualizedComponent.getRange(),
                mutualizedComponent.getCreated(),
                mutualizedComponent.getUpdated(),
                mutualizedComponent.getCreator(),
                mutualizedComponent.getDisseminationStatus(),
                mutualizedComponent.getMinLength(),
                mutualizedComponent.getMaxLength(),
                mutualizedComponent.getMinInclusive(),
                mutualizedComponent.getMaxInclusive(),
                mutualizedComponent.getPattern()
        );

        boolean stringParameters = attributes.equals(actualAttributes);

        Structure[] structureArray= new Structure[]{new Structure(), new Structure()};
        mutualizedComponent.setStructures(structureArray);

        boolean structureParameters = Arrays.equals(structureArray, mutualizedComponent.getStructures());

        List<String> contributors = new ArrayList<>();
        contributors.add("firstContributor");
        contributors.add("secondContributor");
        mutualizedComponent.setContributor(contributors);

        boolean stringArrayParameters = contributors.equals(mutualizedComponent.getContributor());

        assertTrue(stringParameters && structureParameters && stringArrayParameters);
    }
}