package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.graphdb.ObjectType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static fr.insee.rmes.PropertiesKeys.BASE_URI_GESTION;
import static fr.insee.rmes.PropertiesKeys.BASE_URI_PUBLICATION;

@Component
public record BauhausUriBuilder(  String baseUriPublication, String baseUriGestion, PropertiesFinder propertiesFinder) {

    public BauhausUriBuilder(@Value("${"+BASE_URI_PUBLICATION+"}") String baseUriPublication,
                    @Value("${"+BASE_URI_GESTION+"}") String baseUriGestion,
                    PropertiesFinder propertiesFinder){
        this.baseUriPublication=baseUriPublication;
        this.baseUriGestion=baseUriGestion;
        this.propertiesFinder=propertiesFinder;
        RdfUtils.setBauhausUriBuilder(this);
    }

    public String getBaseUriPublication(ObjectType objectType){
        return baseUriPublication + getBaseUri(objectType).orElse("");
    }

    public String getBaseUriGestion(ObjectType objectType) {
        return this.baseUriGestion + getBaseUri(objectType).orElse("");
    }

    /**
     * Get value by URI
     */
    public String getCompleteUriGestion(String labelType, String id) {
        var enumByLabel=ObjectType.getEnumByLabel(labelType);
        return enumByLabel.map(this::getBaseUriGestion).orElse(ObjectType.UNDEFINED.baseUriModifier().apply(""))
                + "/" + id;
    }

    public String getCompleteUriPublication(String labelType, String id) {
        var enumByLabel=ObjectType.getEnumByLabel(labelType);
        return enumByLabel.map(this::getBaseUriPublication).orElse(ObjectType.UNDEFINED.baseUriModifier().apply(""))
                + "/" + id;
    }

    private Optional<String> getBaseUri(ObjectType objectType){
        return propertiesFinder.findByName(objectType.baseUriPropertyName())
                .map(objectType.baseUriModifier());
    }

    public interface PropertiesFinder {
        Optional<String> findByName(String name);
    }
}
