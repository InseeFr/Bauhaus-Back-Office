package fr.insee.rmes.config.swagger.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class IdLabelTwoLangsTest {

    @Test
    void shouldCreateIdLabelTwoLangs(){

        List<String> creators = List.of("unknown","mockedCreator");

        IdLabelTwoLangs idLabelTwoLangs = new IdLabelTwoLangs("mockedID","mockedLabelLg1","mockedLabelLg2");
        IdLabelTwoLangs idLabelTwoLangsOther = new IdLabelTwoLangs();

        idLabelTwoLangsOther.setId("mockedID");
        idLabelTwoLangsOther.setLabelLg1("mockedLabelLg1");
        idLabelTwoLangsOther.setLabelLg2("mockedLabelLg2");
        idLabelTwoLangsOther.setCreators(creators);

        boolean isSameID = Objects.equals(idLabelTwoLangs.getId(), idLabelTwoLangsOther.getId());
        boolean isSameLabelLg1 = Objects.equals(idLabelTwoLangs.getLabelLg1(), idLabelTwoLangsOther.getLabelLg1());
        boolean isSameLabelLg2 = Objects.equals(idLabelTwoLangs.getLabelLg2(), idLabelTwoLangsOther.getLabelLg2());
        boolean isNotSameCreators = idLabelTwoLangs.getCreators()!=idLabelTwoLangsOther.getCreators();
        boolean isSameClass = idLabelTwoLangs.getClass()==idLabelTwoLangsOther.getClass();

        assertTrue(isSameID && isSameLabelLg1 && isSameLabelLg2 && isNotSameCreators && isSameClass);
    }

}