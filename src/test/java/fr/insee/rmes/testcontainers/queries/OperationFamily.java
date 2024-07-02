package fr.insee.rmes.testcontainers.queries;

import fr.insee.rmes.bauhaus_services.OperationsService;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OperationFamily extends WithGraphDBContainer{

    @Autowired
    OperationsService operationService;

    @Test
    void getAllFamilies() throws Exception {
        importTrigFile("all-operations-and-indicators.trig");
        String result = operationService.getFamilies();
        assertEquals(new JSONArray(result).length(), 56);
    }
}
