package fr.insee.rmes.model.structures;

import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StructureTest {

    @Test
    void shouldReturnStructureAttributes() throws RmesException {

        Structure structure = new Structure();

        Structure otherStructure = new Structure("mockedId");

        List<String> attributes = List.of("mockedID",
                "mockedLabelLg1",
                "mockedLabelLg2",
                "mockedDescriptionLg1",
                "mockedDescriptionLg2",
                "mockedCreated",
                "mockedUpdated",
                "mockedCreator",
                "mockedDisseminationStatus",
                "mockedIdentifiant");

        structure.setId(attributes.getFirst());
        structure.setLabelLg1(attributes.get(1));
        structure.setLabelLg2(attributes.get(2));
        structure.setDescriptionLg1(attributes.get(3));
        structure.setDescriptionLg2(attributes.get(4));
        structure.setCreated(attributes.get(5));
        structure.setUpdated(attributes.get(6));
        structure.setCreator(attributes.get(7));
        structure.setDisseminationStatus(attributes.get(8));
        structure.setIdentifiant(attributes.get(9));

        List<String> actualAttributes = List.of(
                structure.getId(),
                structure.getLabelLg1(),
                structure.getLabelLg2(),
                structure.getDescriptionLg1(),
                structure.getDescriptionLg2(),
                structure.getCreated(),
                structure.getUpdated(),
                structure.getCreator(),
                structure.getDisseminationStatus(),
                structure.getIdentifiant());

        boolean stringParameters = attributes.equals(actualAttributes);

        List<String> stringArray= List.of("mockedString","mockedString");
        structure.setContributor(stringArray);

        boolean stringListParameters =stringArray.equals(structure.getContributor());

        List<ComponentDefinition> componentDefinitionArray= List.of(new ComponentDefinition(),new ComponentDefinition());
        structure.setComponentDefinitions(componentDefinitionArray);

        boolean componentDefinitionListParameters =componentDefinitionArray.equals(structure.getComponentDefinitions());

        assertTrue(stringParameters && stringListParameters && componentDefinitionListParameters);

    }
}