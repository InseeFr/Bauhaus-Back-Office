package fr.insee.rmes.model.structures;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ComponentDefinitionTest {

    @Test
    void shouldReturnComponentDefinitionAttributes() throws RmesException {

        ComponentDefinition componentDefinition= new ComponentDefinition();
        componentDefinition.setId("mockedId");
        componentDefinition.setCreated("mockedCreated");
        componentDefinition.setModified("mockedModified");
        componentDefinition.setOrder("mockedOrder");
        String [] strings = {"one", "two","three"};
        componentDefinition.setAttachment(strings);
        componentDefinition.setRequired(true);
        componentDefinition.setLabelLg1("mockedLabelLg1");
        componentDefinition.setLabelLg2("mockedLabelLg2");
        componentDefinition.setNotation("mockedNotation");
        MutualizedComponent component = new MutualizedComponent();
        component.setDescriptionLg1("mockedComponent");
        componentDefinition.setComponent(component);

        List<String> actualStringParameters = List.of(componentDefinition.getId(),
                componentDefinition.getCreated(),
                componentDefinition.getModified(),
                componentDefinition.getOrder(),
                componentDefinition.getNotation(),
                componentDefinition.getLabelLg1(),
                componentDefinition.getLabelLg2()
        );

        List<String> expectedStringList = List.of("mockedId","mockedCreated","mockedModified","mockedOrder","mockedNotation","mockedLabelLg1","mockedLabelLg2"
        );
        boolean stringParameters = expectedStringList.equals(actualStringParameters);
        boolean stringArrayParameter = strings==componentDefinition.getAttachment();
        boolean mutualizedComponentParameter = component==componentDefinition.getComponent();
        boolean booleanParameter = componentDefinition.getRequired();

        Assertions.assertTrue(stringParameters && stringArrayParameter && mutualizedComponentParameter && booleanParameter);
    }
}