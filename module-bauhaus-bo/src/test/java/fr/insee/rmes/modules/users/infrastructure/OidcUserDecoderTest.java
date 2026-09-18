package fr.insee.rmes.modules.users.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.exceptions.EmptyUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class OidcUserDecoderTest {

    @Mock
    private OrganisationsService organisationsService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JwtProperties.RoleClaim roleClaimConfig;

    @Mock
    private Jwt jwt;

    private OidcUserDecoder userDecoder;
    private RoleClaimExtractor roleClaimExtractor;

    @BeforeEach
    void setUp() throws OrganisationFetchException {
        lenient().when(jwtProperties.getIdClaim()).thenReturn("sub");
        lenient().when(jwtProperties.getStampClaim()).thenReturn("timbre");
        lenient().when(jwtProperties.getSourceClaim()).thenReturn("source");
        lenient().when(jwtProperties.getRoleClaim()).thenReturn("roles");
        lenient().when(jwtProperties.getRoleClaimConfig()).thenReturn(roleClaimConfig);
        lenient().when(roleClaimConfig.getRoles()).thenReturn("roles");

        lenient().when(organisationsService.getAdmsIdentifier(anyString())).thenReturn(Optional.empty());
        lenient().when(organisationsService.getDctermsIdentifier(anyString())).thenReturn(Optional.empty());

        roleClaimExtractor = new RoleClaimExtractor(jwtProperties);
        userDecoder = new OidcUserDecoder(organisationsService, jwtProperties, roleClaimExtractor);
    }

    private Map<String, Object> buildRolesClaim(List<String> roles) {
        Map<String, Object> rolesMap = new HashMap<>();
        rolesMap.put("roles", roles);
        return rolesMap;
    }

    /** Claims of user "user123"; a null stamp or source leaves the corresponding claim out. */
    private Map<String, Object> buildClaims(String stamp, String source, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        if (stamp != null) {
            claims.put("timbre", stamp);
        }
        if (source != null) {
            claims.put("source", source);
        }
        claims.put("roles", buildRolesClaim(roles));
        return claims;
    }

    private void givenInseeGroupClaim() {
        when(jwtProperties.getInseeGroupClaim()).thenReturn("groups");
        when(jwtProperties.getHieApplicationPrefix()).thenReturn("APP");
    }

    private User decode(Map<String, Object> claims) throws MissingUserInformationException {
        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        return result.get();
    }

    @Test
    void should_decode_jwt_with_all_claims() throws MissingUserInformationException {
        Map<String, Object> claims = buildClaims("STAMP-01", "insee", List.of("ADMIN", "USER"));

        User user = decode(claims);

        assertThat(user.id()).isEqualTo("user123");
        assertThat(user.getStamps()).containsExactly("STAMP-01");
        assertThat(user.roles()).containsExactly("ADMIN", "USER");
    }

    @Test
    void should_return_empty_for_anonymous_user() throws MissingUserInformationException {
        Optional<User> result = userDecoder.fromPrincipal("anonymousUser");

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_user_when_principal_is_already_user() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN"), Set.of("STAMP-01"), "insee");

        Optional<User> result = userDecoder.fromPrincipal(user);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void should_return_empty_for_unknown_principal_type() throws MissingUserInformationException {
        Optional<User> result = userDecoder.fromPrincipal(new Object());

        assertThat(result).isEmpty();
    }

    @Test
    void should_throw_exception_when_claims_are_empty() {
        when(jwt.getClaims()).thenReturn(Map.of());

        assertThatThrownBy(() -> userDecoder.fromPrincipal(jwt)).isInstanceOf(EmptyUserInformationException.class);
    }

    @Test
    void should_extract_stamp_from_insee_groups() throws MissingUserInformationException {
        givenInseeGroupClaim();

        Map<String, Object> claims = buildClaims(null, "insee", List.of("ADMIN"));
        claims.put("groups", List.of("GROUP1", "STAMP-02_APP", "GROUP2"));
        // No stamp in stampClaim, should extract from groups

        User user = decode(claims);

        assertThat(user.getStamps()).containsExactly("STAMP-02");
    }

    @Test
    void should_prefer_stamp_claim_over_insee_groups() throws MissingUserInformationException {
        Map<String, Object> claims = buildClaims("STAMP-DIRECT", "insee", List.of("ADMIN"));
        claims.put("groups", List.of("STAMP-02_APP"));

        User user = decode(claims);

        assertThat(user.getStamps()).containsExactly("STAMP-DIRECT");
    }

    @Test
    void should_handle_empty_roles() throws MissingUserInformationException {
        Map<String, Object> claims = buildClaims("STAMP-01", "insee", List.of());

        User user = decode(claims);

        assertThat(user.roles()).isEmpty();
    }

    @Test
    void should_handle_null_source() throws MissingUserInformationException {
        // No source claim
        Map<String, Object> claims = buildClaims("STAMP-01", null, List.of("USER"));

        User user = decode(claims);

        assertThat(user.source()).isNull();
    }

    @Test
    void should_extract_multiple_roles() throws MissingUserInformationException {
        Map<String, Object> claims = buildClaims("STAMP-01", "ssm", List.of("ADMIN", "USER", "MODERATOR"));

        User user = decode(claims);

        assertThat(user.roles()).containsExactly("ADMIN", "USER", "MODERATOR");
    }

    @Test
    void should_extract_first_matching_group_from_insee_groups() throws MissingUserInformationException {
        givenInseeGroupClaim();

        Map<String, Object> claims = buildClaims(null, "insee", List.of("USER"));
        claims.put("groups", List.of("GROUP1", "STAMP-FIRST_APP", "STAMP-SECOND_APP"));

        User user = decode(claims);

        // Should extract the first matching group, with the application suffix stripped
        assertThat(user.getStamps()).containsAnyOf("STAMP-FIRST", "STAMP-SECOND");
    }

    @Test
    void should_add_adms_identifier_when_stamp_claim_present()
            throws MissingUserInformationException, OrganisationFetchException {
        when(organisationsService.getAdmsIdentifier("DG75-F601")).thenReturn(Optional.of("HIE3000165"));

        User user = decode(buildClaims("DG75-F601", "insee", List.of("USER")));

        assertThat(user.getStamps()).containsExactlyInAnyOrder("DG75-F601", "HIE3000165");
    }

    @Test
    void should_add_dcterms_identifier_when_insee_group_present()
            throws MissingUserInformationException, OrganisationFetchException {
        givenInseeGroupClaim();
        // The application suffix must be stripped before looking up the organisation:
        // GraphDB stores the HIE code (adms:identifier) without the "_APP" suffix.
        when(organisationsService.getDctermsIdentifier("HIE3000165")).thenReturn(Optional.of("DG75-F601"));

        Map<String, Object> claims = buildClaims(null, "insee", List.of("USER"));
        claims.put("groups", List.of("HIE3000165_APP"));

        User user = decode(claims);

        assertThat(user.getStamps()).containsExactlyInAnyOrder("HIE3000165", "DG75-F601");
    }

    @Test
    void should_handle_organisation_service_exception_gracefully()
            throws MissingUserInformationException, OrganisationFetchException {
        when(organisationsService.getAdmsIdentifier("STAMP-01")).thenThrow(new OrganisationFetchException());

        User user = decode(buildClaims("STAMP-01", "insee", List.of("USER")));

        assertThat(user.getStamps()).containsExactly("STAMP-01");
    }
}
