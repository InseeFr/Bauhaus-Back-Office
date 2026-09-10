package fr.insee.rmes.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * Conteneur GraphDB partagé par toute la suite d'intégration (voir {@link WithGraphDBContainer}).
 *
 * <p>Comme le conteneur n'est démarré qu'une fois, l'isolation entre classes de tests ne vient plus
 * de son redémarrage mais de {@link #resetTestData()}, appelé avant chaque classe.
 *
 * <p>Le chargement des fixtures passe par le port HTTP publié plutôt que par {@code docker exec curl} :
 * un exec coûte plusieurs centaines de millisecondes et impose de copier le fichier dans le conteneur
 * au préalable, pour un résultat identique.
 */
public class GraphDBContainer extends GenericContainer<GraphDBContainer> implements SparqlFixtureLoader {
    public static final String DOCKER_ENTRYPOINT_INITDB = "/docker-entrypoint-initdb";

    /** Dossier de fixtures utilisé par défaut, restauré à chaque {@link #resetTestData()}. */
    public static final String DEFAULT_INIT_FOLDER = "/testcontainers";

    /** Dépôt « gestion » créé au démarrage par {@code config.ttl} et cible des fixtures TriG. */
    public static final String GESTION_REPOSITORY = "bauhaus-test";

    static final int GRAPHDB_PORT = 7200;

    /** Extrait l'identifiant du dépôt d'un fichier de configuration RDF4J, pour pouvoir le purger ensuite. */
    private static final Pattern REPOSITORY_ID = Pattern.compile("repository#repositoryID>\\s*\"([^\"]+)\"");

    private final HttpClient httpClient =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    /** Dépôts déjà créés dans ce conteneur : sert à la fois à ne pas les recréer et à savoir quoi purger. */
    private final Set<String> repositories = new LinkedHashSet<>();

    private String folder = DEFAULT_INIT_FOLDER;

    public GraphDBContainer(final String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(GRAPHDB_PORT);
        // GraphDB accepte les connexions sur 7200 bien avant de savoir y répondre : sans cette
        // attente sur l'API REST, la première requête resterait bloquée ~17 s le temps du démarrage.
        waitingFor(Wait.forHttp("/rest/repositories")
                .forPort(GRAPHDB_PORT)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(3)));
    }

    @Override
    public void start() {
        super.start();
        withInitFolder(DEFAULT_INIT_FOLDER);
        withRepository("config.ttl");
    }

    @Override
    public GraphDBContainer withInitFolder(String folder) {
        this.folder = folder;
        return this;
    }

    /**
     * Crée le dépôt décrit par un fichier de configuration RDF4J, sauf s'il existe déjà. Plusieurs
     * classes de tests demandent le même dépôt de publication : sur un conteneur partagé, seule la
     * première demande a un effet.
     */
    public GraphDBContainer withRepository(String ttlFile) {
        String repositoryId = repositoryIdOf(ttlFile);
        if (!repositories.add(repositoryId)) {
            return this;
        }
        try {
            String path = copyFile(ttlFile);
            execInContainer(
                    "curl",
                    "-X",
                    "POST",
                    "-H",
                    "Content-Type:multipart/form-data",
                    "-F",
                    "config=@" + path,
                    "http://localhost:7200/rest/repositories");
        } catch (IOException | InterruptedException _) {
            throw new AssertionError("The TTL file was not loaded");
        }
        return this;
    }

    @Override
    public GraphDBContainer withTrigFiles(String file) {
        post(
                "/repositories/" + GESTION_REPOSITORY + "/statements",
                "application/x-trig",
                readFixture(file),
                "The Trig file was not loaded");
        return this;
    }

    /**
     * Ramène le conteneur dans l'état où chaque classe de tests le trouvait quand il était redémarré
     * pour elle : tous les dépôts vides et le dossier de fixtures par défaut.
     */
    public void resetTestData() {
        withInitFolder(DEFAULT_INIT_FOLDER);
        repositories.forEach(repository -> post(
                "/repositories/" + repository + "/statements",
                "application/sparql-update",
                "CLEAR ALL".getBytes(StandardCharsets.UTF_8),
                "The repository " + repository + " was not cleared"));
    }

    private void post(String path, String contentType, byte[] body, String failureMessage) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + getHost() + ":" + getMappedPort(GRAPHDB_PORT) + path))
                .header("Content-Type", contentType)
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(failureMessage, e);
        } catch (IOException e) {
            throw new AssertionError(failureMessage, e);
        }
        if (response.statusCode() >= 300) {
            throw new AssertionError(
                    failureMessage + " (HTTP " + response.statusCode() + " : " + response.body() + ")");
        }
    }

    private byte[] readFixture(String file) {
        String resource = classpathResource(file);
        try (InputStream stream = GraphDBContainer.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new AssertionError("Fixture " + resource + " not found on the test classpath");
            }
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String repositoryIdOf(String ttlFile) {
        String configuration = new String(readFixture(ttlFile), StandardCharsets.UTF_8);
        Matcher matcher = REPOSITORY_ID.matcher(configuration);
        if (!matcher.find()) {
            throw new AssertionError("No repositoryID found in " + ttlFile);
        }
        return matcher.group(1);
    }

    private String classpathResource(String file) {
        String resource = this.folder + "/" + file;
        return resource.startsWith("/") ? resource.substring(1) : resource;
    }

    private String copyFile(String file) throws IOException, InterruptedException {
        String fullPath = DOCKER_ENTRYPOINT_INITDB + "/" + file;
        copyFileToContainer(MountableFile.forClasspathResource(this.folder + "/" + file), fullPath);
        assertThatFileExists(file);
        return fullPath;
    }

    private void assertThatFileExists(String file) throws IOException, InterruptedException {
        Container.ExecResult lsResult = execInContainer("ls", "-al", DOCKER_ENTRYPOINT_INITDB);
        String stdout = lsResult.getStdout();
        assertThat(stdout)
                .withFailMessage(
                        "Expecting file %1$s to be in folder %2$s of container", file, DOCKER_ENTRYPOINT_INITDB)
                .contains(file);
    }
}
