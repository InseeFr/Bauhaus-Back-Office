package fr.insee.rmes.testcontainers.queries;

import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.operations.OperationsImplStubContainer;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
@Tag("integration")
class OperationFamilyTest extends WithGraphDBContainer{

    OperationsService operationService=new OperationsImplStubContainer(getRdfGestionConnectionDetails());

    @Test
    void getAllFamilies() throws Exception {
        container.withTrigFiles("all-operations-and-indicators.trig");
        OpSeriesQueries.setConfig(new ConfigStub());
        var families = operationService.getFamilies();
        assertEquals(56, families.size());
    }
}
