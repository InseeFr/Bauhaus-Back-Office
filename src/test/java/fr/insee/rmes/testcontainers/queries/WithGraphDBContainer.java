package fr.insee.rmes.testcontainers.queries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfConnectionDetails;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


@Testcontainers
public class WithGraphDBContainer {
    @Container
    public static GraphDBContainer container = new GraphDBContainer(getDockerImageName());

    private static String getDockerImageName() {
        Path filePath = Paths.get(System.getProperty("user.dir"), "compose.yaml");
        File file = filePath.toFile();

        String serviceName = "graphdb";

        YAMLMapper yamlMapper = new YAMLMapper();
        JsonNode rootNode = null;
        try {
            rootNode = yamlMapper.readTree(file);
            JsonNode servicesNode = rootNode.get("services");
            if (servicesNode != null && servicesNode.has(serviceName)) {
                JsonNode serviceNode = servicesNode.get(serviceName);
                JsonNode imageNode = serviceNode.get("image");
                if (imageNode != null) {
                    return imageNode.asText();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "";
    }


    protected static RdfConnectionDetails getRdfGestionConnectionDetails() {
        return new RdfConnectionDetails() {
            @Override
            public String getUrlServer() {
                return "http://" + container.getHost() + ":" + container.getMappedPort(7200);
            }

            @Override
            public String repositoryId() {
                return "bauhaus-test";
            }
        };
    }

}
