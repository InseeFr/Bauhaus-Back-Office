package fr.insee.rmes.testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


@Testcontainers
public class WithGraphDBContainer {

    @Container
    public static final GraphDBContainer container = new GraphDBContainer("ontotext/graphdb:10.8.4");
    public static final String BAUHAUS_TEST_REPOSITORY = "bauhaus-test";


    protected static RdfConnectionDetails getRdfGestionConnectionDetails() {
        return new RdfConnectionDetails() {
            @Override
            public String getUrlServer() {
                return "http://" + container.getHost() + ":" + container.getMappedPort(7200);
            }

            @Override
            public String repositoryId() {
                return BAUHAUS_TEST_REPOSITORY;
            }
        };
    }

}