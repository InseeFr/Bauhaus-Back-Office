package fr.insee.rmes.config.swagger.model.code_list;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PageTest {

    @Test
    void shouldCheckAttributesDefaultValues(){
        Page page = new Page();
        List<Boolean> actual = List.of(page.getPage()==0,page.getTotal()==0,page.getItems()==null);
        List<Boolean> expected = List.of(true,true,true);
        assertEquals(expected,actual);
    }
}