package fr.insee.rmes.config;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConfigStubTest {

@Test
void shouldInitializeNonNullVariableConfigStub(){
    ConfigStub configStub = new ConfigStub();
    boolean isNotNullLg2 = configStub.getLg2()!=null;
    boolean isNotNullLg1 = configStub.getLg1()!=null;
    boolean isNotOperationGraph = configStub.getOperationsGraph()!=null;
    boolean isNotNullOrganizationsGraph = configStub.getOrganizationsGraph()!=null;
    boolean isNotNullProductsGraph = configStub.getProductsGraph()!=null;
    boolean isNotNullOrgInseeGraph = configStub.getOrgInseeGraph()!=null;
    boolean isNotNullStructuresComponentsGraph = configStub.getStructuresComponentsGraph()!=null;
    boolean isNotNullStructuresGraph = configStub.getStructuresGraph()!=null;
    boolean isNotNullCodeListGraph = configStub.getCodeListGraph()!=null;

    List<Boolean> actual = List.of(
            isNotNullLg2,
            isNotNullLg1,
            isNotOperationGraph,
            isNotNullOrganizationsGraph,
            isNotNullProductsGraph,
            isNotNullOrgInseeGraph,
            isNotNullStructuresComponentsGraph,
            isNotNullStructuresGraph,
            isNotNullCodeListGraph
    );

    assertEquals(actual,List.of(true,true,true,true,true,true,true,true,true));

}

}