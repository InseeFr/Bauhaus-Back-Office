package fr.insee.rmes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentsStorageProperties {

    private final String storageGestion;
    private final String baseUrl;

    public DocumentsStorageProperties(
            @Value("${fr.insee.rmes.bauhaus.storage.document.gestion}") String storageGestion,
            @Value("${fr.insee.web4g.baseURL}") String baseUrl) {
        this.storageGestion = storageGestion;
        this.baseUrl = baseUrl;
    }

    public String storageGestion() {
        return storageGestion;
    }

    public String baseUrl() {
        return baseUrl.trim();
    }
}
