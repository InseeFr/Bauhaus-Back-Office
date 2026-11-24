package fr.insee.rmes.config;

import fr.insee.rmes.DocumentationsProperties;

public class DocumentationsPropertiesStub {

    private DocumentationsPropertiesStub() {}

    public static DocumentationsProperties stub() {
        return new DocumentationsProperties("", "", "concepts/definitions/scheme");
    }
}
