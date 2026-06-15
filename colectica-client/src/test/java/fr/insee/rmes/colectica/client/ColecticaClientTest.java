package fr.insee.rmes.colectica.client;

import fr.insee.rmes.colectica.client.auth.ColecticaCredentials;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaSetItem;
import fr.insee.rmes.colectica.client.dto.GetDescriptionsRequest;
import fr.insee.rmes.colectica.client.dto.UpdateItemStateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ColecticaClientTest {

    private static final String BASE_API_URL = "http://colectica.example.com/api/v1/";
    private static final String BASE_SERVER_URL = "http://colectica.example.com";
    private static final String TOKEN = "test-token-123";
    private static final String LOGICAL_PRODUCT_TYPE = "965c8d28-7d48-4950-bea7-04b27e52bb9b";

    private record Fixture(ColecticaClient client, MockRestServiceServer server) {}

    /** Fixture whose token is a fixed supplier returning {@link #TOKEN}. */
    private Fixture newFixture() {
        return newFixture(new ColecticaCredentials.BearerToken(() -> TOKEN));
    }

    private Fixture newFixture(ColecticaCredentials credentials) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(
            new ColecticaClient(builder.build(), BASE_API_URL, BASE_SERVER_URL, credentials), server);
    }

    @Test
    void query_postsItemTypesWithBearerTokenAndMapsResponse() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(jsonPath("$.itemTypes[0]").value(LOGICAL_PRODUCT_TYPE))
            .andExpect(jsonPath("$.searchLatestVersion").value(true))
            .andRespond(withSuccess(
                "{\"Results\":[{\"Identifier\":\"lp-1\",\"AgencyId\":\"fr.insee\"}],"
                    + "\"TotalResults\":1,\"ReturnedResults\":1}",
                MediaType.APPLICATION_JSON));

        ColecticaResponse response = f.client.query(List.of(LOGICAL_PRODUCT_TYPE));

        f.server.verify();
        assertThat(response.totalResults()).isEqualTo(1);
        assertThat(response.results().get(0).identifier()).isEqualTo("lp-1");
    }

    @Test
    void getDescriptions_postsIdentifiersAndMapsItems() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "item/_getList"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(jsonPath("$.identifiers[0].identifier").value("pi-1"))
            .andRespond(withSuccess(
                "[{\"Identifier\":\"pi-1\",\"AgencyId\":\"fr.insee\",\"Version\":1,\"Item\":\"<x/>\","
                    + "\"IsPublished\":true,\"IsDeprecated\":false,\"IsProvisional\":false}]",
                MediaType.APPLICATION_JSON));

        ColecticaItemResponse[] items = f.client.getDescriptions(
            List.of(new GetDescriptionsRequest.IdentifierRef("fr.insee", "pi-1", 1)));

        f.server.verify();
        assertThat(items).hasSize(1);
        assertThat(items[0].identifier()).isEqualTo("pi-1");
        assertThat(items[0].item()).isEqualTo("<x/>");
    }

    @Test
    void getItem_getsUrlEncodedItemWithOptionalVersion() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "item/fr.insee/pi-1/2"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andRespond(withSuccess(
                "{\"Identifier\":\"pi-1\",\"AgencyId\":\"fr.insee\",\"Version\":2,"
                    + "\"IsPublished\":true,\"IsDeprecated\":false,\"IsProvisional\":false}",
                MediaType.APPLICATION_JSON));

        ColecticaItemResponse item = f.client.getItem("fr.insee", "pi-1", "2");

        f.server.verify();
        assertThat(item.identifier()).isEqualTo("pi-1");
        assertThat(item.version()).isEqualTo(2);
    }

    @Test
    void getSet_getsSetReferencesWithoutVersion() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "set/fr.insee/su-1"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andRespond(withSuccess(
                "[{\"Item1\":\"lp-1\",\"Item2\":1,\"Item3\":\"fr.insee\"}]",
                MediaType.APPLICATION_JSON));

        ColecticaSetItem[] set = f.client.getSet("fr.insee", "su-1", null);

        f.server.verify();
        assertThat(set).hasSize(1);
        assertThat(set[0].identifier()).isEqualTo("lp-1");
    }

    @Test
    void getDdiSet_returnsRawBytes() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "ddiset/fr.insee/g-1"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andRespond(withSuccess("<FragmentInstance/>".getBytes(StandardCharsets.UTF_8),
                MediaType.APPLICATION_OCTET_STREAM));

        byte[] bytes = f.client.getDdiSet("fr.insee", "g-1");

        f.server.verify();
        assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("<FragmentInstance/>");
    }

    @Test
    void createOrUpdateItems_postsRequestToItemEndpoint() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "item"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(jsonPath("$.Items[0].Identifier").value("pi-1"))
            .andExpect(jsonPath("$.options.namedOptions[0]").value("RegisterOrReplace"))
            .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

        ColecticaItemResponse item = new ColecticaItemResponse(
            "type", "fr.insee", 1, "pi-1", "<x/>", null, null, false, false, false, null);
        String response = f.client.createOrUpdateItems(new ColecticaCreateItemRequest(List.of(item)));

        f.server.verify();
        assertThat(response).isEqualTo("ok");
    }

    @Test
    void updateItemState_postsStateToUpdateStateEndpoint() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "item/_updateState"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(jsonPath("$.state").value(true))
            .andExpect(jsonPath("$.ids[0].identifier").value("g-1"))
            .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

        UpdateItemStateRequest request = new UpdateItemStateRequest(
            List.of(new UpdateItemStateRequest.ItemIdentifier("fr.insee", "g-1", 1)), true, true);
        String response = f.client.updateItemState(request);

        f.server.verify();
        assertThat(response).isEqualTo("ok");
    }

    @Test
    void findRelatedDescriptions_postsFilteredQueryAndMapsResponse() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "_query/relationship/bysubject/descriptions"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(jsonPath("$.itemTypes[0]").value(LOGICAL_PRODUCT_TYPE))
            .andExpect(jsonPath("$.targetItem.identifier").value("su-1"))
            .andRespond(withSuccess(
                "[{\"AgencyId\":\"fr.insee\",\"Identifier\":\"lp-1\"}]",
                MediaType.APPLICATION_JSON));

        List<ItemReference> result = f.client.findRelatedDescriptions(
            RelationshipDirection.BY_SUBJECT,
            new ItemReference("fr.insee", "su-1"),
            List.of(LOGICAL_PRODUCT_TYPE));

        f.server.verify();
        assertThat(result).containsExactly(new ItemReference("fr.insee", "lp-1"));
    }

    @Test
    void findRelatedDescriptions_returnsEmptyListWhenNoRelatedItem() {
        Fixture f = newFixture();
        f.server.expect(requestTo(BASE_API_URL + "_query/relationship/bysubject/descriptions"))
            .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<ItemReference> result = f.client.findRelatedDescriptions(
            RelationshipDirection.BY_SUBJECT, new ItemReference("fr.insee", "su-empty"),
            List.of(LOGICAL_PRODUCT_TYPE));

        assertThat(result).isEmpty();
    }

    // --- authentication / token management ---

    @Test
    void userPassword_obtainsTokenFromCreateTokenThenCallsApi() {
        Fixture f = newFixture(new ColecticaCredentials.UserPassword("user", "secret"));

        f.server.expect(requestTo(BASE_SERVER_URL + "/token/createtoken"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.username").value("user"))
            .andExpect(jsonPath("$.password").value("secret"))
            .andRespond(withSuccess("{\"access_token\":\"jwt-abc\"}", MediaType.APPLICATION_JSON));
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer jwt-abc"))
            .andRespond(withSuccess("{\"TotalResults\":0,\"ReturnedResults\":0,\"Results\":[]}",
                MediaType.APPLICATION_JSON));

        f.client.query(List.of(LOGICAL_PRODUCT_TYPE));

        f.server.verify();
    }

    @Test
    void userPassword_reauthenticatesAndRetriesOnceOnUnauthorized() {
        Fixture f = newFixture(new ColecticaCredentials.UserPassword("user", "secret"));

        // 1) initial token
        f.server.expect(requestTo(BASE_SERVER_URL + "/token/createtoken"))
            .andRespond(withSuccess("{\"access_token\":\"expired\"}", MediaType.APPLICATION_JSON));
        // 2) API call rejected with the expired token
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer expired"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        // 3) re-authentication
        f.server.expect(requestTo(BASE_SERVER_URL + "/token/createtoken"))
            .andRespond(withSuccess("{\"access_token\":\"fresh\"}", MediaType.APPLICATION_JSON));
        // 4) retry succeeds with the fresh token
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer fresh"))
            .andRespond(withSuccess("{\"TotalResults\":0,\"ReturnedResults\":0,\"Results\":[]}",
                MediaType.APPLICATION_JSON));

        ColecticaResponse response = f.client.query(List.of(LOGICAL_PRODUCT_TYPE));

        f.server.verify();
        assertThat(response).isNotNull();
    }

    @Test
    void userPassword_cachesTokenAcrossCalls() {
        Fixture f = newFixture(new ColecticaCredentials.UserPassword("user", "secret"));

        f.server.expect(requestTo(BASE_SERVER_URL + "/token/createtoken"))
            .andRespond(withSuccess("{\"access_token\":\"jwt-abc\"}", MediaType.APPLICATION_JSON));
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer jwt-abc"))
            .andRespond(withSuccess("{\"TotalResults\":0,\"ReturnedResults\":0,\"Results\":[]}",
                MediaType.APPLICATION_JSON));
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer jwt-abc"))
            .andRespond(withSuccess("{\"TotalResults\":0,\"ReturnedResults\":0,\"Results\":[]}",
                MediaType.APPLICATION_JSON));

        f.client.query(List.of(LOGICAL_PRODUCT_TYPE));
        f.client.query(List.of(LOGICAL_PRODUCT_TYPE));

        // Only ONE /token/createtoken across the two API calls.
        f.server.verify();
    }

    @Test
    void bearerTokenSupplier_isQueriedPerCallAndRefreshedOnUnauthorized() {
        AtomicInteger calls = new AtomicInteger();
        Supplier<String> supplier = () -> "tok-" + calls.incrementAndGet();
        Fixture f = newFixture(new ColecticaCredentials.BearerToken(supplier));

        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer tok-1"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer tok-2"))
            .andRespond(withSuccess("{\"TotalResults\":0,\"ReturnedResults\":0,\"Results\":[]}",
                MediaType.APPLICATION_JSON));

        f.client.query(List.of(LOGICAL_PRODUCT_TYPE));

        f.server.verify();
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void bearerToken_runsInvalidateHookOnUnauthorizedSoSupplierIssuesFreshToken() {
        // Models a caching supplier: the same token is returned until onInvalidate() is run.
        AtomicInteger version = new AtomicInteger(1);
        AtomicInteger invalidations = new AtomicInteger();
        Supplier<String> supplier = () -> "tok-" + version.get();
        Runnable onInvalidate = () -> {
            invalidations.incrementAndGet();
            version.incrementAndGet();
        };
        Fixture f = newFixture(new ColecticaCredentials.BearerToken(supplier, onInvalidate));

        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer tok-1"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        f.server.expect(requestTo(BASE_API_URL + "_query"))
            .andExpect(header("Authorization", "Bearer tok-2"))
            .andRespond(withSuccess("{\"TotalResults\":0,\"ReturnedResults\":0,\"Results\":[]}",
                MediaType.APPLICATION_JSON));

        f.client.query(List.of(LOGICAL_PRODUCT_TYPE));

        f.server.verify();
        assertThat(invalidations.get()).isEqualTo(1);
    }
}
