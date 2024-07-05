package fr.insee.rmes.testcontainers.queries;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class WithGraphDBContainer {
    static GenericContainer container = new GenericContainer("ontotext/graphdb:10.6.4")
            .withExposedPorts(7200);

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        container.start();

        container.copyFileToContainer(MountableFile.forClasspathResource("/testcontainers"), "/opt/graphdb/home/testcontainers");
        container.copyFileToContainer(MountableFile.forClasspathResource("/trig"), "/opt/graphdb/home/trig");

        Container.ExecResult lsResult = container.execInContainer("ls", "-al", "/opt/graphdb/home/testcontainers/");
        String stdout = lsResult.getStdout();
        int exitCode = lsResult.getExitCode();
        assertThat(stdout).contains("config.ttl");
        assertThat(exitCode).isZero();

        container.execInContainer("curl", "-X", "POST", "-H", "Content-Type:multipart/form-data", "-F", "config=@/opt/graphdb/home/testcontainers/config.ttl", "http://localhost:7200/rest/repositories");
    }

    @AfterAll
    static void afterAll() {
        container.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> "bauhaus-test");
    }

    public static void importTrigFile(String path) throws IOException, InterruptedException {
        container.execInContainer("curl", "-X", "POST", "-H", "Content-Type: application/x-trig", "--data-binary", "@/opt/graphdb/home/trig/" + path, "http://localhost:7200/repositories/bauhaus-test/statements");
    }
}
