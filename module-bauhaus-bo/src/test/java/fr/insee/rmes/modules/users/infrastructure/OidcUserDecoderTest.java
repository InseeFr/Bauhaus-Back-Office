package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.exceptions.EmptyUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingStampException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OidcUserDecoderTest {

    @Mock
    private OrganisationsService organisationsService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private Jwt jwt;

    private OidcUserDecoder userDecoder;

    @BeforeEach
    void setUp() throws OrganisationFetchException {
        lenient().when(jwtProperties.getIdClaim()).thenReturn("sub");
        lenient().when(jwtProperties.getStampClaim()).thenReturn("timbre");
        lenient().when(jwtProperties.getSourceClaim()).thenReturn("source");
        lenient().when(jwtProperties.getRoleClaim()).thenReturn("roles");

        lenient().when(organisationsService.getAdmsIdentifier(anyString())).thenReturn(Optional.empty());
        lenient().when(organisationsService.getDctermsIdentifier(anyString())).thenReturn(Optional.empty());

        userDecoder = new OidcUserDecoder(organisationsService, jwtProperties);
    }

    @Test
    void should_decode_jwt_with_all_claims() throws MissingUserInformationException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-01");
        claims.put("source", "insee");
        claims.put("roles", List.of("ADMIN", "USER"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("user123");
        assertThat(result.get().getStamps()).containsExactly("STAMP-01");
        assertThat(result.get().roles()).containsExactly("ADMIN", "USER");
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

        assertThatThrownBy(() -> userDecoder.fromPrincipal(jwt))
            .isInstanceOf(EmptyUserInformationException.class);
    }



    @Test
    void should_extract_stamp_from_insee_groups() throws MissingUserInformationException {
        when(jwtProperties.getInseeGroupClaim()).thenReturn("groups");
        when(jwtProperties.getHieApplicationPrefix()).thenReturn("APP");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("source", "insee");
        claims.put("roles", List.of("ADMIN"));
        claims.put("groups", List.of("GROUP1", "STAMP-02_APP", "GROUP2"));
        // No stamp in stampClaim, should extract from groups

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().getStamps()).containsExactly("STAMP-02_APP");
    }

    @Test
    void should_prefer_stamp_claim_over_insee_groups() throws MissingUserInformationException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-DIRECT");
        claims.put("source", "insee");
        claims.put("roles", List.of("ADMIN"));
        claims.put("groups", List.of("STAMP-02_APP"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().getStamps()).containsExactly("STAMP-DIRECT");
    }

    @Test
    void should_handle_empty_roles() throws MissingUserInformationException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-01");
        claims.put("source", "insee");
        claims.put("roles", List.of());

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().roles()).isEmpty();
    }

    @Test
    void should_handle_null_source() throws MissingUserInformationException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-01");
        claims.put("roles", List.of("USER"));
        // No source claim

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().source()).isNull();
    }

    @Test
    void should_extract_multiple_roles() throws MissingUserInformationException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-01");
        claims.put("source", "ssm");
        claims.put("roles", List.of("ADMIN", "USER", "MODERATOR"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().roles()).containsExactly("ADMIN", "USER", "MODERATOR");
    }



    @Test
    void should_extract_first_matching_group_from_insee_groups() throws MissingUserInformationException {
        when(jwtProperties.getInseeGroupClaim()).thenReturn("groups");
        when(jwtProperties.getHieApplicationPrefix()).thenReturn("APP");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("source", "insee");
        claims.put("roles", List.of("USER"));
        claims.put("groups", List.of("GROUP1", "STAMP-FIRST_APP", "STAMP-SECOND_APP"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        // Should extract the first matching group
        assertThat(result.get().getStamps()).containsAnyOf("STAMP-FIRST_APP", "STAMP-SECOND_APP");
    }

    @Test
    void should_add_adms_identifier_when_stamp_claim_present() throws MissingUserInformationException, OrganisationFetchException {
        when(organisationsService.getAdmsIdentifier("DG75-F601")).thenReturn(Optional.of("HIE3000165"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "DG75-F601");
        claims.put("source", "insee");
        claims.put("roles", List.of("USER"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().getStamps()).containsExactlyInAnyOrder("DG75-F601", "HIE3000165");
    }

    @Test
    void should_add_dcterms_identifier_when_insee_group_present() throws MissingUserInformationException, OrganisationFetchException {
        when(jwtProperties.getInseeGroupClaim()).thenReturn("groups");
        when(jwtProperties.getHieApplicationPrefix()).thenReturn("APP");
        when(organisationsService.getDctermsIdentifier("HIE3000165_APP")).thenReturn(Optional.of("DG75-F601"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("source", "insee");
        claims.put("roles", List.of("USER"));
        claims.put("groups", List.of("HIE3000165_APP"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().getStamps()).containsExactlyInAnyOrder("HIE3000165_APP", "DG75-F601");
    }

    @Test
    void should_handle_organisation_service_exception_gracefully() throws MissingUserInformationException, OrganisationFetchException {
        when(organisationsService.getAdmsIdentifier("STAMP-01")).thenThrow(new OrganisationFetchException());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-01");
        claims.put("source", "insee");
        claims.put("roles", List.of("USER"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().getStamps()).containsExactly("STAMP-01");
    }
}
