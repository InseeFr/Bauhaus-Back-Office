package fr.insee.rmes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BauhausUriProperties {

    private final String baseUriGestion;
    private final String linksBaseUri;
    private final String codeListBaseUri;
    private final String documentsBaseUri;
    private final String productsBaseUri;

    public BauhausUriProperties(
            @Value("${" + PropertiesKeys.BASE_URI_GESTION + "}") String baseUriGestion,
            @Value("${" + PropertiesKeys.LINKS_BASE_URI + "}") String linksBaseUri,
            @Value("${" + PropertiesKeys.CODE_LIST_BASE_URI + "}") String codeListBaseUri,
            @Value("${" + PropertiesKeys.DOCUMENTS_BASE_URI + "}") String documentsBaseUri,
            @Value("${" + PropertiesKeys.PRODUCTS_BASE_URI + "}") String productsBaseUri) {
        this.baseUriGestion = baseUriGestion;
        this.linksBaseUri = linksBaseUri;
        this.codeListBaseUri = codeListBaseUri;
        this.documentsBaseUri = documentsBaseUri;
        this.productsBaseUri = productsBaseUri;
    }

    public String baseUriGestion() {
        return baseUriGestion;
    }

    public String linksBaseUri() {
        return linksBaseUri;
    }

    public String codeListBaseUri() {
        return codeListBaseUri;
    }

    public String documentsBaseUri() {
        return documentsBaseUri;
    }

    public String productsBaseUri() {
        return productsBaseUri;
    }
}
