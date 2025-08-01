package fr.insee.rmes.config.swagger.model.code_list;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CodeLabelListTest {

    @Test
    void shouldCheckAttributesDefaultValues(){

        CodeLabelList codeLabelList = new CodeLabelList();
        List<Boolean> actual = List.of(codeLabelList.code==null,
                codeLabelList.labelLg1==null,
                codeLabelList.labelLg2==null,
                codeLabelList.notationCodeList==null);

        List<Boolean> expected = List.of(true,true,true,true);

        assertEquals(expected,actual);

    }

}