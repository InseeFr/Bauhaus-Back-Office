package fr.insee.rmes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentationsProperties {

    private final String titlePrefixLg1;
    private final String titlePrefixLg2;
    private final String conceptsScheme;

    public DocumentationsProperties(
            @Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg1}") String titlePrefixLg1,
            @Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg2}") String titlePrefixLg2,
            @Value("${fr.insee.rmes.bauhaus.concepts.scheme}") String conceptsScheme) {
        this.titlePrefixLg1 = titlePrefixLg1;
        this.titlePrefixLg2 = titlePrefixLg2;
        this.conceptsScheme = conceptsScheme;
    }

    public String titlePrefixLg1() {
        return titlePrefixLg1;
    }

    public String titlePrefixLg2() {
        return titlePrefixLg2;
    }

    public String conceptsScheme() {
        return conceptsScheme;
    }
}
