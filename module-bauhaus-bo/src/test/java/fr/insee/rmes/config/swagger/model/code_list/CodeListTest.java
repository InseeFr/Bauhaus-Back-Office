package fr.insee.rmes.config.swagger.model.code_list;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class CodeListTest {

    @Test
    void shouldCheckAttributesDefaultValues(){
        CodeList codeList = new CodeList();
        List<Boolean> actual = List.of(codeList.notation==null,
                codeList.labelLg1==null,
                codeList.labelLg2==null,
                codeList.codes==null,
                codeList.range==null,
                codeList.uri==null,
                codeList.id==null,
                codeList.iri==null,
                codeList.creator==null,
                codeList.created==null,
                codeList.lastListUriSegment==null,
                codeList.lastClassUriSegment==null,
                codeList.disseminationStatus==null,
                codeList.modified==null,
                codeList.validationState==null,
                codeList.descriptionLg1==null,
                codeList.descriptionLg2==null,
                codeList.lastCodeUriSegment==null,
                codeList.contributor==null
                );
        List<Boolean> expected = List.of(true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true);
        assertEquals(expected,actual);
    }

    @Test
    void shouldCheckAttributesForConstructorWithParameters(){

        CodeList codeList = new CodeList("mockedNotation");

        List<Boolean> actual = List.of(Objects.equals(codeList.getNotation(), "mockedNotation"),
                codeList.getIri()==null);

        List<Boolean> expected = List.of(true,true);
        assertEquals(expected,actual);
    }

}