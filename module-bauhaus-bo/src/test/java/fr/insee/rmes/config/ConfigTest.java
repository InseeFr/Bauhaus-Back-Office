package fr.insee.rmes.config;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

@Test
    void shouldInitializeSomeNullVariableWhenConfig(){
    Config config = new Config();
    boolean isNullLg2 = config.getLg2()==null;
    boolean isNullLg1 = config.getLg1()==null;
    boolean isNotOperationGraph = config.getOperationsGraph()!=null;
    boolean isNotNullOrganizationsGraph = config.getOrganizationsGraph()!=null;
    boolean isNotNullProductsGraph = config.getProductsGraph()!=null;
    boolean isNotNullOrgInseeGraph = config.getOrgInseeGraph()!=null;
    boolean isNotNullStructuresComponentsGraph = config.getStructuresComponentsGraph()!=null;
    boolean isNotNullStructuresGraph = config.getStructuresGraph()!=null;
    boolean isNotNullCodeListGraph = config.getCodeListGraph()!=null;
    boolean isNotNullDocumentsStoragePublicationInterne = config.getDocumentsStoragePublicationInterne()!=null;

    List<Boolean> actual = List.of(
    isNullLg2,
    isNullLg1,
    isNotOperationGraph,
    isNotNullOrganizationsGraph,
    isNotNullProductsGraph,
    isNotNullOrgInseeGraph,
    isNotNullStructuresComponentsGraph,
    isNotNullStructuresGraph,
    isNotNullCodeListGraph,
    isNotNullDocumentsStoragePublicationInterne
    );

    assertEquals(actual,List.of(true,true,true,true,true,true,true,true,true,false));

}

}