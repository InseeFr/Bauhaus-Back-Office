package fr.insee.rmes.colectica.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Journalise en DEBUG les corps envoyés à Colectica et reçus en retour (méthode, URL, statut), le JSON
 * indenté, sous le logger de {@link ColecticaClient}. Hors DEBUG, la réponse traverse l'intercepteur sans
 * être lue. L'endpoint de jeton n'est jamais journalisé : sa requête porte le mot de passe, sa réponse
 * le jeton d'accès.
 */
class ColecticaResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaClient.class);
    private static final String TOKEN_PATH = "/token/";
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        if (!logger.isDebugEnabled() || request.getURI().getPath().contains(TOKEN_PATH)) {
            return response;
        }
        BufferedResponse buffered = new BufferedResponse(response);
        // Colectica n'indique pas de charset : décodage UTF-8 explicite, comme pour getDdiSet.
        logger.debug(
                "Colectica {} {} -> {}\n>>> request body:\n{}\n<<< response body:\n{}",
                request.getMethod(),
                request.getURI(),
                buffered.getStatusCode().value(),
                pretty(body),
                pretty(buffered.content));
        return buffered;
    }

    /** Le corps indenté s'il s'agit de JSON, sinon tel quel (XML du ddiset, corps vide d'un GET…). */
    private static String pretty(byte[] content) {
        String text = new String(content, StandardCharsets.UTF_8);
        if (text.isBlank()) {
            return text;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readTree(text));
        } catch (JacksonException notJson) {
            return text;
        }
    }

    /** Réponse dont le corps, lu une fois pour le journal, reste relisible par les convertisseurs. */
    private static final class BufferedResponse implements ClientHttpResponse {

        private final ClientHttpResponse delegate;
        private final byte[] content;

        private BufferedResponse(ClientHttpResponse delegate) throws IOException {
            this.delegate = delegate;
            this.content = delegate.getBody().readAllBytes();
        }

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
