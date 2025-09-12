package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;

class FreeMarkerUtilsTest {

    @Test
    void buildRequestTest() throws RmesException {
        assertThatCode(()->FreeMarkerUtils.buildRequest("", "getAllGraphs.ftlh", Map.of()))
                .doesNotThrowAnyException();
        assertThat(FreeMarkerUtils.buildRequest("", "getAllGraphs.ftlh", Map.of()))
                .contains("""
                        SELECT DISTINCT ?g
                        WHERE {
                          GRAPH ?g {?a ?b ?c }
                        }""");
    }

    @Test
    void buildRequestTest_xdocreport() {
        //if not null, you should define a TemplateLoader for freemarker templates in xdocreport in FreemarkerConfig
        assertNull(FreeMarkerUtilsTest.class.getClassLoader().getResource("xdocreport"));
        assertNotNull(FreeMarkerUtilsTest.class.getClassLoader().getResource("request"));
    }

}