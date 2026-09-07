package fr.insee.rmes.colectica.client;

import fr.insee.rmes.colectica.client.auth.ColecticaCredentials;
import fr.insee.rmes.colectica.client.dto.AuthenticationRequest;
import fr.insee.rmes.colectica.client.dto.AuthenticationResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaResponse;
import fr.insee.rmes.colectica.client.dto.QueryAdvancedRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import fr.insee.rmes.colectica.client.dto.QueryRequest;
import fr.insee.rmes.colectica.client.dto.UpdateItemStateRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * Thin SDK over the Colectica Repository REST API.
 *
 * <p>The client owns the only {@link RestClient} used to talk to Colectica <em>and</em> manages
 * authentication: it is built with {@link ColecticaCredentials} (user/password or a bearer-token
 * supplier), obtains and caches the token itself, and transparently re-authenticates and retries once
 * on a 401/403. Callers never deal with tokens.
 *
 * <p>{@code baseApiUrl} is the API root (e.g. {@code https://host/api/v1/}); {@code baseServerUrl} is
 * the server root used by the token endpoint (e.g. {@code https://host}).
 */
public class ColecticaClient {

    private static final String BEARER_PREFIX = "Bearer ";

    private final RestClient restClient;
    private final String baseApiUrl;
    private final String baseServerUrl;
    private final ColecticaCredentials credentials;

    private volatile String cachedToken;

    public ColecticaClient(
        RestClient restClient,
        String baseApiUrl,
        String baseServerUrl,
        ColecticaCredentials credentials
    ) {
        this.restClient = restClient;
        this.baseApiUrl = baseApiUrl;
        this.baseServerUrl = baseServerUrl;
        this.credentials = credentials;
    }

    // --- public API (no token parameter: auth is handled internally) ---

    /**
     * Searches items by type via {@code POST _query} (latest version).
     */
    public ColecticaResponse query(List<String> itemTypes) {
        return withAuth(token -> restClient
            .post()
            .uri(baseApiUrl + "_query")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .body(new QueryRequest(itemTypes))
            .retrieve()
            .body(ColecticaResponse.class));
    }

    /**
     * Searches items by type via {@code POST _query/advanced} (latest version), asking Colectica to
     * include all per-item properties. Unlike {@link #query(List)}, the response carries the rich
     * property bags — notably {@code DateProperties.versionDate} — see {@link ColecticaAdvancedItem}.
     */
    public ColecticaAdvancedResponse queryAdvanced(List<String> itemTypes) {
        return withAuth(token -> restClient
            .post()
            .uri(baseApiUrl + "_query/advanced")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .body(new QueryAdvancedRequest(itemTypes))
            .retrieve()
            .body(ColecticaAdvancedResponse.class));
    }

    /**
     * Batch-fetches full items (with their XML) for the given identifiers via {@code POST item/_getList}.
     */
    public ColecticaItemResponse[] getDescriptions(List<GetDescriptionsRequest.IdentifierRef> identifiers) {
        return withAuth(token -> restClient
            .post()
            .uri(baseApiUrl + "item/_getList")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .body(new GetDescriptionsRequest(identifiers))
            .retrieve()
            .body(ColecticaItemResponse[].class));
    }

    /**
     * Fetches a single item via {@code GET item/{agency}/{id}[/{version}]} (URL-encoded segments).
     */
    public ColecticaItemResponse getItem(String agency, String id, String version) {
        String url = baseApiUrl + "item/"
            + URLEncoder.encode(agency, StandardCharsets.UTF_8) + "/"
            + URLEncoder.encode(id, StandardCharsets.UTF_8);
        if (version != null && !version.isBlank()) {
            url += "/" + URLEncoder.encode(version, StandardCharsets.UTF_8);
        }
        String finalUrl = url;
        return withAuth(token -> restClient
            .get()
            .uri(finalUrl)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .retrieve()
            .body(ColecticaItemResponse.class));
    }

    /**
     * Deletes every version of an item via {@code DELETE item/{agency}/{id}} (URL-encoded segments).
     */
    public void deleteItem(String agency, String id) {
        String url = baseApiUrl + "item/"
            + URLEncoder.encode(agency, StandardCharsets.UTF_8) + "/"
            + URLEncoder.encode(id, StandardCharsets.UTF_8);
        withAuth(token -> restClient
            .delete()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .retrieve()
            .toBodilessEntity());
    }

    /**
     * Fetches the references of a set via {@code GET set/{agency}/{id}[/{version}]} (latest when version
     * is {@code null}/blank).
     */
    public ColecticaSetItem[] getSet(String agency, String id, String version) {
        String url = baseApiUrl + "set/" + agency + "/" + id;
        if (version != null && !version.isBlank()) {
            url += "/" + version;
        }
        String finalUrl = url;
        return withAuth(token -> restClient
            .get()
            .uri(finalUrl)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .retrieve()
            .body(ColecticaSetItem[].class));
    }

    /**
     * Fetches the full DDI set as raw bytes via {@code GET ddiset/{agency}/{id}}. Returned as bytes so
     * the caller can decode UTF-8 explicitly (Colectica omits the charset and Spring would fall back to
     * ISO-8859-1).
     */
    public byte[] getDdiSet(String agency, String id) {
        return withAuth(token -> restClient
            .get()
            .uri(baseApiUrl + "ddiset/" + agency + "/" + id)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .retrieve()
            .body(byte[].class));
    }

    /**
     * Creates or updates items via {@code POST item} (RegisterOrReplace). Returns the raw response body.
     */
    public String createOrUpdateItems(ColecticaCreateItemRequest request) {
        return withAuth(token -> restClient
            .post()
            .uri(baseApiUrl + "item")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .body(request)
            .retrieve()
            .body(String.class));
    }

    /**
     * Updates the state of items (e.g. deprecation) via {@code POST item/_updateState}.
     */
    public String updateItemState(UpdateItemStateRequest request) {
        return withAuth(token -> restClient
            .post()
            .uri(baseApiUrl + "item/_updateState")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .body(request)
            .retrieve()
            .body(String.class));
    }

    /**
     * Returns the items directly related to {@code target} in the given {@code direction}, filtered
     * server-side by {@code itemTypes}, via {@code POST _query/relationship/{direction}/descriptions}.
     *
     * <p>The response carries only item references (agency + identifier), not their content — this is
     * the lightweight alternative to fetching a whole {@code set/} and downloading every item.
     *
     * @return the matching item references, never {@code null} (empty when none)
     */
    public List<ItemReference> findRelatedDescriptions(
        RelationshipDirection direction,
        ItemReference target,
        List<String> itemTypes
    ) {
        String url = baseApiUrl + "_query/relationship/" + direction.urlSegment() + "/descriptions";
        RelationshipQuery query = new RelationshipQuery(
            itemTypes,
            new RelationshipQuery.TargetItem(target.agencyId(), target.identifier())
        );
        ItemReference[] response = withAuth(token -> restClient
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .body(query)
            .retrieve()
            .body(ItemReference[].class));
        return response == null ? List.of() : List.of(response);
    }

    /**
     * Same {@code _query/relationship/.../descriptions} call as {@link #findRelatedDescriptions}, but
     * keeps the full description objects (with their {@code ItemName}/{@code Label} dictionaries)
     * instead of collapsing them to bare {@link ItemReference}s. Use this when you need the related
     * items' labels: it avoids a separate repository-wide label query, since the descriptions endpoint
     * already returns them.
     */
    public List<ColecticaItem> findRelatedItems(
        RelationshipDirection direction,
        ItemReference target,
        List<String> itemTypes
    ) {
        String url = baseApiUrl + "_query/relationship/" + direction.urlSegment() + "/descriptions";
        RelationshipQuery query = new RelationshipQuery(
            itemTypes,
            new RelationshipQuery.TargetItem(target.agencyId(), target.identifier())
        );
        ColecticaItem[] response = withAuth(token -> restClient
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
            .body(query)
            .retrieve()
            .body(ColecticaItem[].class));
        return response == null ? List.of() : List.of(response);
    }

    // --- authentication / token management ---

    /**
     * Runs {@code call} with the current bearer token. On a 401/403, invalidates the token, obtains a
     * fresh one and retries once.
     */
    private <T> T withAuth(Function<String, T> call) {
        try {
            return call.apply(currentToken());
        } catch (HttpClientErrorException e) {
            HttpStatusCode status = e.getStatusCode();
            if (status.value() == 401 || status.value() == 403) {
                invalidateToken();
                return call.apply(currentToken());
            }
            throw e;
        }
    }

    private String currentToken() {
        return switch (credentials) {
            case ColecticaCredentials.BearerToken bearer -> bearer.tokenSupplier().get();
            case ColecticaCredentials.UserPassword userPassword -> {
                String token = cachedToken;
                if (token == null) {
                    token = authenticate(userPassword.username(), userPassword.password());
                    cachedToken = token;
                }
                yield token;
            }
        };
    }

    private void invalidateToken() {
        switch (credentials) {
            case ColecticaCredentials.UserPassword ignored -> cachedToken = null;
            // Let the supplier drop its cached token so the retry re-queries a fresh one.
            case ColecticaCredentials.BearerToken bearer -> bearer.onInvalidate().run();
        }
    }

    private String authenticate(String username, String password) {
        AuthenticationResponse response = restClient
            .post()
            .uri(baseServerUrl + "/token/createtoken")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new AuthenticationRequest(username, password))
            .retrieve()
            .body(AuthenticationResponse.class);
        if (response == null || response.accessToken() == null) {
            throw new RuntimeException("Authentication failed: unable to retrieve access token");
        }
        return response.accessToken();
    }
}
