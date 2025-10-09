package fr.insee.rmes.testcontainers;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphDBContainer extends GenericContainer<GraphDBContainer> {
    public static final String DOCKER_ENTRYPOINT_INITDB = "/docker-entrypoint-initdb";
    private String folder;

    public GraphDBContainer(final String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(7200);
    }

    @Override
    public void start() {
        super.start();
        withInitFolder("/testcontainers").withExposedPorts(7200);
        withRepository("config.ttl");
    }

    public GraphDBContainer withInitFolder(String folder){
        this.folder = folder;
        return this;
    }

    public GraphDBContainer withRepository(String ttlFile) {
        try {
            String path = copyFile(ttlFile);
            execInContainer("curl", "-X", "POST", "-H", "Content-Type:multipart/form-data", "-F", "config=@" + path, "http://localhost:7200/rest/repositories");
        } catch (IOException | InterruptedException _) {
            throw new AssertionError("The TTL file was not loaded");
        }
        return this;
    }

    public GraphDBContainer withTrigFiles(String file) {
        try {
            String path = copyFile(file);
            execInContainer("curl", "-X", "POST", "-H", "Content-Type: application/x-trig", "--data-binary", "@" + path, "http://localhost:7200/repositories/bauhaus-test/statements");
        } catch (IOException | InterruptedException _) {
            throw new AssertionError("The Trig file was not loaded");
        }
        return this;
    }

    private String copyFile(String file) throws IOException, InterruptedException {
        String fullPath = DOCKER_ENTRYPOINT_INITDB  + "/" + file;
        copyFileToContainer(MountableFile.forClasspathResource(this.folder + "/" + file), fullPath);
        assertThatFileExists(file);
        return fullPath;
    }

    private void assertThatFileExists(String file) throws IOException, InterruptedException {
        Container.ExecResult lsResult = execInContainer("ls", "-al", DOCKER_ENTRYPOINT_INITDB);
        String stdout = lsResult.getStdout();
        assertThat(stdout).contains(file).withFailMessage("Expecting file %1$s to be in folder %2$s of container", file, DOCKER_ENTRYPOINT_INITDB);
    }
}