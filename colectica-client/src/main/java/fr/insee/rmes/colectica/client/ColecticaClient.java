package fr.insee.rmes.colectica.client;

import fr.insee.rmes.colectica.client.auth.ColecticaCredentials;
import fr.insee.rmes.colectica.client.dto.AuthenticationRequest;
import fr.insee.rmes.colectica.client.dto.AuthenticationResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.colectica.client.dto.ColecticaTypedSetItem;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import fr.insee.rmes.colectica.client.dto.GetLatestItemsRequest;
import fr.insee.rmes.colectica.client.dto.QueryAdvancedRequest;
import fr.insee.rmes.colectica.client.dto.QueryRequest;
import fr.insee.rmes.colectica.client.dto.SetQueryRequest;
import fr.insee.rmes.colectica.client.dto.UpdateItemStateRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

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
            RestClient restClient, String baseApiUrl, String baseServerUrl, ColecticaCredentials credentials) {
        this.restClient = restClient
                .mutate()
                .requestInterceptor(new ColecticaResponseLoggingInterceptor())
                .build();
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

    /** Plafond de résultats demandé à {@code _query} quand toute une population est attendue. */
    public static final int MAX_QUERY_RESULTS = 1_000_000;

    /**
     * Searches, via {@code POST _query} (latest version), the items of the given types belonging to the
     * set rooted at {@code setRoot}. Envelopes only — labels, names, versions — without the items' XML:
     * far lighter than {@code set/} followed by {@code item/_getList} on a large set.
     */
    public ColecticaResponse queryInSet(List<String> itemTypes, ColecticaSetItem setRoot) {
        QueryRequest request = new QueryRequest(
                itemTypes,
                true,
                List.of(new QueryRequest.SearchSet(setRoot.agencyId(), setRoot.identifier(), setRoot.version())),
                MAX_QUERY_RESULTS);
        return withAuth(token -> restClient
                .post()
                .uri(baseApiUrl + "_query")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .body(request)
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
     * Batch-fetches the latest version of each referenced item via {@code POST item/_getListLatest}.
     * Identifiers unknown to Colectica are silently left out of the response.
     */
    public ColecticaItemResponse[] getLatestItems(List<ItemReference> references) {
        return withAuth(token -> restClient
                .post()
                .uri(baseApiUrl + "item/_getListLatest")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .body(new GetLatestItemsRequest(references))
                .retrieve()
                .body(ColecticaItemResponse[].class));
    }

    /**
     * Resolves the latest version number of each referenced item via
     * {@code POST item/_getLatestVersionNumbers}, without fetching the items' content. Colectica answers
     * {@code null} for an unknown identifier: such entries are dropped.
     */
    public List<ColecticaSetItem> getLatestVersionNumbers(List<ItemReference> references) {
        ColecticaSetItem[] response = withAuth(token -> restClient
                .post()
                .uri(baseApiUrl + "item/_getLatestVersionNumbers")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .body(new GetLatestItemsRequest(references))
                .retrieve()
                .body(ColecticaSetItem[].class));
        return nonNullEntries(response);
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
     * Walks the set of {@code root} like {@link #getSet}, but keeps only the items of the given types,
     * filtered server-side, via {@code POST _query/set}. {@code root} must carry an actual version: with
     * version {@code 0} Colectica returns an empty result.
     */
    public ColecticaTypedSetItem[] querySet(ColecticaSetItem root, List<String> itemTypes) {
        SetQueryRequest query = new SetQueryRequest(
                new SetQueryRequest.RootItem(root.agencyId(), root.identifier(), root.version()),
                new SetQueryRequest.Facet(itemTypes, false, true, true));
        return withAuth(token -> restClient
                .post()
                .uri(baseApiUrl + "_query/set")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .body(query)
                .retrieve()
                .body(ColecticaTypedSetItem[].class));
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
     * <p>The relationships of <em>every</em> version of {@code target} are returned: an item removed
     * by a later version still comes back. Use {@link #findRelatedDescriptions(RelationshipDirection,
     * ItemReference, int, List)} to read a single version.
     *
     * @return the matching item references, never {@code null} (empty when none)
     */
    public List<ItemReference> findRelatedDescriptions(
            RelationshipDirection direction, ItemReference target, List<String> itemTypes) {
        return relationshipDescriptions(
                direction, RelationshipQuery.allVersions(itemTypes, target), ItemReference[].class);
    }

    /**
     * Same as {@link #findRelatedDescriptions(RelationshipDirection, ItemReference, List)}, restricted
     * to the relationships of {@code version} of {@code target}.
     */
    public List<ItemReference> findRelatedDescriptions(
            RelationshipDirection direction, ItemReference target, int version, List<String> itemTypes) {
        return relationshipDescriptions(
                direction, RelationshipQuery.ofVersion(itemTypes, target, version), ItemReference[].class);
    }

    /**
     * Same {@code _query/relationship/.../descriptions} call as {@link #findRelatedDescriptions}, but
     * keeps the full description objects (with their {@code ItemName}/{@code Label} dictionaries)
     * instead of collapsing them to bare {@link ItemReference}s. Use this when you need the related
     * items' labels: it avoids a separate repository-wide label query, since the descriptions endpoint
     * already returns them.
     */
    public List<ColecticaItem> findRelatedItems(
            RelationshipDirection direction, ItemReference target, List<String> itemTypes) {
        return relationshipDescriptions(
                direction, RelationshipQuery.allVersions(itemTypes, target), ColecticaItem[].class);
    }

    /**
     * Same as {@link #findRelatedItems(RelationshipDirection, ItemReference, List)}, restricted to the
     * relationships of {@code version} of {@code target}.
     */
    public List<ColecticaItem> findRelatedItems(
            RelationshipDirection direction, ItemReference target, int version, List<String> itemTypes) {
        return relationshipDescriptions(
                direction, RelationshipQuery.ofVersion(itemTypes, target, version), ColecticaItem[].class);
    }

    private <T> List<T> relationshipDescriptions(
            RelationshipDirection direction, RelationshipQuery query, Class<T[]> responseType) {
        String url = baseApiUrl + "_query/relationship/" + direction.urlSegment() + "/descriptions";
        T[] response = withAuth(token -> restClient
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .body(query)
                .retrieve()
                .body(responseType));
        return nonNullEntries(response);
    }

    /**
     * The relationship {@code descriptions} endpoints may return {@code null} entries (a relationship
     * pointing at an item that can no longer be resolved, e.g. a deleted one): they are dropped.
     */
    private static <T> List<T> nonNullEntries(T[] response) {
        return response == null
                ? List.of()
                : Arrays.stream(response).filter(Objects::nonNull).toList();
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
            case ColecticaCredentials.BearerToken bearer ->
                bearer.tokenSupplier().get();
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
            case ColecticaCredentials.BearerToken bearer ->
                bearer.onInvalidate().run();
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
