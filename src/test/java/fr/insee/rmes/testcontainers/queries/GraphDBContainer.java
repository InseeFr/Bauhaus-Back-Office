package fr.insee.rmes.testcontainers.queries;

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

    public GraphDBContainer withInitFolder(String folder){
        this.folder = folder;
        return this;
    }

    public GraphDBContainer withRepository(String ttlFile) throws IOException, InterruptedException {
        String path = copyFile(ttlFile);
        execInContainer("curl", "-X", "POST", "-H", "Content-Type:multipart/form-data", "-F", "config=@" + path, "http://localhost:7200/rest/repositories");
        return this;
    }

    public GraphDBContainer withTrigFiles(String file) throws IOException, InterruptedException {
        String path = copyFile(file);
        execInContainer("curl", "-X", "POST", "-H", "Content-Type: application/x-trig", "--data-binary", "@" + path, "http://localhost:7200/repositories/bauhaus-test/statements");
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
        int exitCode = lsResult.getExitCode();
        assertThat(stdout).contains(file);
        assertThat(exitCode).isZero();
    }
}
