package fr.insee.rmes.modules.users.infrastructure;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleClaimExtractorTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JwtProperties.RoleClaim roleClaimConfig;

    private RoleClaimExtractor roleClaimExtractor;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getRoleClaim()).thenReturn("roles");
        lenient().when(jwtProperties.getRoleClaimConfig()).thenReturn(roleClaimConfig);
        lenient().when(roleClaimConfig.getRoles()).thenReturn("roles");

        roleClaimExtractor = new RoleClaimExtractor(jwtProperties);
    }

    @Test
    void should_extract_roles_from_map_with_list() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("roles", List.of("ADMIN", "USER"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", nestedMap);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).containsExactly("ADMIN", "USER");
    }

    @Test
    void should_extract_roles_from_json_object_with_json_array() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("ADMIN");
        jsonArray.add("USER");
        jsonArray.add("MODERATOR");

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("roles", jsonArray);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", jsonObject);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).containsExactly("ADMIN", "USER", "MODERATOR");
    }

    @Test
    void should_extract_roles_from_map_with_nested_key() {
        when(jwtProperties.getRoleClaim()).thenReturn("realm_access");

        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("roles", List.of("ROLE_ADMIN", "ROLE_USER"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", nestedMap);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).containsExactly("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void should_extract_roles_from_json_object_with_nested_key() {
        when(jwtProperties.getRoleClaim()).thenReturn("realm_access");

        JsonArray rolesArray = new JsonArray();
        rolesArray.add("ROLE_ADMIN");
        rolesArray.add("ROLE_USER");

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("roles", rolesArray);

        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", jsonObject);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).containsExactly("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void should_return_empty_stream_when_role_claim_is_absent() {
        Map<String, Object> claims = new HashMap<>();

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).isEmpty();
    }

    @Test
    void should_return_empty_stream_when_role_claim_is_null() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", null);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).isEmpty();
    }

    @Test
    void should_return_empty_stream_for_empty_list() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("roles", List.of());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", nestedMap);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).isEmpty();
    }

    @Test
    void should_return_empty_stream_for_empty_json_array() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("roles", new JsonArray());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", jsonObject);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).isEmpty();
    }

    @Test
    void should_return_empty_stream_for_unsupported_type() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "not_a_list_or_array");

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).isEmpty();
    }

    @Test
    void should_handle_single_role() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("roles", List.of("ADMIN"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", nestedMap);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).containsExactly("ADMIN");
    }

    @Test
    void should_handle_many_roles() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("roles", List.of("ROLE_1", "ROLE_2", "ROLE_3", "ROLE_4", "ROLE_5"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", nestedMap);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).containsExactly("ROLE_1", "ROLE_2", "ROLE_3", "ROLE_4", "ROLE_5");
    }

    @Test
    void should_return_empty_stream_when_nested_key_is_missing_in_map() {
        when(jwtProperties.getRoleClaim()).thenReturn("realm_access");
        when(roleClaimConfig.getRoles()).thenReturn("missing_key");

        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("roles", List.of("ADMIN"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", nestedMap);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).isEmpty();
    }

    @Test
    void should_extract_roles_with_special_characters() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("roles", List.of("ROLE:ADMIN", "ROLE-USER", "ROLE_MODERATOR"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", nestedMap);

        List<String> roles = roleClaimExtractor.extractRoles(claims).toList();

        assertThat(roles).containsExactly("ROLE:ADMIN", "ROLE-USER", "ROLE_MODERATOR");
    }
}