package fr.insee.rmes.testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Conteneur Fuseki partagé par les tests d'intégration (voir {@link WithFusekiContainer}), pendant de
 * {@code GraphDBContainer}.
 *
 * <p>Le dataset {@value #DATASET} est créé au démarrage en envoyant un fichier d'assembleur à l'API
 * d'administration, et non par {@code FUSEKI_DATASET_1} : c'est la seule façon de lui donner
 * {@code tdb2:unionDefaultGraph true}. L'assembleur ne peut pas non plus être copié dans
 * {@code $FUSEKI_BASE/configuration} avant le démarrage : le dossier créé par la copie appartient à
 * root, et Fuseki refuse de démarrer sur un dossier de configuration qu'il ne peut pas écrire.
 */
public class FusekiContainer extends GenericContainer<FusekiContainer> implements SparqlFixtureLoader {

    /** Dossier de fixtures utilisé par défaut, restauré à chaque {@link #resetTestData()}. */
    public static final String DEFAULT_INIT_FOLDER = "/testcontainers";

    /** Dataset créé au démarrage et cible des fixtures TriG. */
    public static final String DATASET = "bauhaus";

    static final int FUSEKI_PORT = 3030;

    /** Identifiants de test : l'image protège par authentification les écritures et l'administration. */
    public static final String ADMIN_USER = "admin";

    public static final String ADMIN_PASSWORD = "admin";

    private final HttpClient httpClient =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private String folder = DEFAULT_INIT_FOLDER;

    public FusekiContainer(final String dockerImageName) {
        super(dockerImageName);
        withExposedPorts(FUSEKI_PORT);
        withEnv("ADMIN_PASSWORD", ADMIN_PASSWORD);
        // L'image ouvre son port avant que l'API d'administration ne réponde : on attend celle-ci, qui
        // sert juste après à créer le dataset.
        waitingFor(Wait.forHttp("/$/datasets")
                .forPort(FUSEKI_PORT)
                .withBasicCredentials(ADMIN_USER, ADMIN_PASSWORD)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(2)));
    }

    @Override
    public void start() {
        super.start();
        post(
                getServerUrl() + "/$/datasets",
                "text/turtle",
                readClasspathResource("fuseki/" + DATASET + ".ttl"),
                "The dataset " + DATASET + " was not created");
    }

    public String getServerUrl() {
        return "http://" + getHost() + ":" + getMappedPort(FUSEKI_PORT);
    }

    /** URL du dataset, à laquelle s'ajoutent {@code /sparql}, {@code /update} et {@code /data}. */
    public String getDatasetUrl() {
        return getServerUrl() + "/" + DATASET;
    }

    @Override
    public FusekiContainer withInitFolder(String folder) {
        this.folder = folder;
        return this;
    }

    @Override
    public FusekiContainer withTrigFiles(String file) {
        String resource = folder + "/" + file;
        post(
                getDatasetUrl() + "/data",
                "application/trig",
                readClasspathResource(resource.startsWith("/") ? resource.substring(1) : resource),
                "The Trig file was not loaded");
        return this;
    }

    /** Vide le dataset et restaure le dossier de fixtures par défaut. */
    public void resetTestData() {
        withInitFolder(DEFAULT_INIT_FOLDER);
        post(
                getDatasetUrl() + "/update",
                "application/sparql-update",
                "CLEAR ALL".getBytes(StandardCharsets.UTF_8),
                "The dataset " + DATASET + " was not cleared");
    }

    private void post(String url, String contentType, byte[] body, String failureMessage) {
        String credentials = Base64.getEncoder()
                .encodeToString((ADMIN_USER + ":" + ADMIN_PASSWORD).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", contentType)
                .header("Authorization", "Basic " + credentials)
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

    private static byte[] readClasspathResource(String resource) {
        try (InputStream stream = FusekiContainer.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new AssertionError("Resource " + resource + " not found on the classpath");
            }
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
