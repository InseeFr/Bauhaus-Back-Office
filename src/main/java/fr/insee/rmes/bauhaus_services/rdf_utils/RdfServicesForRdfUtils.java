package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.config.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public record RdfServicesForRdfUtils(Config config, UriUtils uriUtils) {

    @PostConstruct
    public void initRdfUtils() {
        RdfUtils.instance=this;
    }

    String getBaseGraph(){
        return config.getBaseGraph();
    }

    String conceptGraph(){
        return config.getConceptsGraph();
    }

     String documentsGraph() {
        return config.getDocumentsGraph();
    }

     String operationsGraph(){
        return config.getOperationsGraph();
    }

     String productsGraph(){
        return config.getProductsGraph();
    }

     String documentationsGraph(String id) {
        return config.getDocumentationsGraph() +"/"+ id;
    }


     String documentationsGeoGraph(){
        return config.getDocumentationsGeoGraph();
    }

     String structureGraph(){
        return config.getStructuresGraph();
    }
     String codesListGraph(){
        return config.getCodeListGraph();
    }
     String codesListGraph(String id) {
        return config.getCodeListGraph() + "/" + id;
    }
     String classificationSerieIRI(String id) {
        return config.getBaseUriGestion() + "codes/serieDeNomenclatures/" + id;
    }

     String structureComponentGraph(){
        return config.getStructuresComponentsGraph();
    }

     String conceptScheme(){
        return config.getBaseUriGestion() + config.getConceptsScheme();
    }

    public String getBaseUriGestion(ObjectType objType, String... ids) {
        ArrayList<String> elementsList = new ArrayList<>(List.of(ids));
        elementsList.addFirst(uriUtils.getBaseUriGestion(objType));
        return String.join("/", elementsList.toArray(String[]::new));
    }

    public String getBaseUriPublication(ObjectType objType, String id) {
        return uriUtils.getBaseUriPublication(objType) + "/" + id;
    }

}
