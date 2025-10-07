package fr.insee.rmes.config.swagger.model.code_list;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CodeLabelTwoLangsTest {

    @Test
    void shouldCheckAttributesDefaultValues(){

        CodeLabelTwoLangs codeLabelTwoLangs = new CodeLabelTwoLangs();
        List<Boolean> actual = List.of(codeLabelTwoLangs.code==null,
                codeLabelTwoLangs.labelLg1==null,
                codeLabelTwoLangs.labelLg2==null);

        List<Boolean> expected = List.of(true,true,true);

        assertEquals(expected,actual);

    }

}