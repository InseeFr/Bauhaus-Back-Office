package fr.insee.rmes.modules.users.infrastructure;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OidcUserDecoderTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private Jwt jwt;

    private OidcUserDecoder userDecoder;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getIdClaim()).thenReturn("sub");
        lenient().when(jwtProperties.getStampClaim()).thenReturn("timbre");
        lenient().when(jwtProperties.getSourceClaim()).thenReturn("source");
        lenient().when(jwtProperties.getRoleClaim()).thenReturn("roles");

        userDecoder = new OidcUserDecoder(jwtProperties);
    }

    @Test
    void shouldDecodeJwtWithAllClaims() throws MissingUserInformationException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-01");
        claims.put("source", "insee");
        claims.put("roles", List.of("ADMIN", "USER"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("user123");
        assertThat(result.get().getStamp()).isEqualTo("STAMP-01");
        assertThat(result.get().roles()).containsExactly("ADMIN", "USER");
    }

    @Test
    void shouldReturnEmptyForAnonymousUser() throws MissingUserInformationException {
        Optional<User> result = userDecoder.fromPrincipal("anonymousUser");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnUserWhenPrincipalIsAlreadyUser() throws MissingUserInformationException {
        var user = new User("user123", List.of("ADMIN"), "STAMP-01", "insee");

        Optional<User> result = userDecoder.fromPrincipal(user);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void shouldReturnEmptyForUnknownPrincipalType() throws MissingUserInformationException {
        Optional<User> result = userDecoder.fromPrincipal(new Object());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenClaimsAreEmpty() {
        when(jwt.getClaims()).thenReturn(Map.of());

        assertThatThrownBy(() -> userDecoder.fromPrincipal(jwt))
            .isInstanceOf(EmptyUserInformationException.class);
    }

    @Test
    void shouldThrowExceptionWhenStampIsMissing() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("source", "insee");
        claims.put("roles", List.of("ADMIN"));
        // No stamp claim

        when(jwt.getClaims()).thenReturn(claims);

        assertThatThrownBy(() -> userDecoder.fromPrincipal(jwt))
            .isInstanceOf(MissingStampException.class)
            .hasMessageContaining("user123");
    }

    @Test
    void shouldExtractStampFromInseeGroups() throws MissingUserInformationException {
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
        assertThat(result.get().getStamp()).isEqualTo("STAMP-02_APP");
    }

    @Test
    void shouldPreferStampClaimOverInseeGroups() throws MissingUserInformationException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("timbre", "STAMP-DIRECT");
        claims.put("source", "insee");
        claims.put("roles", List.of("ADMIN"));
        claims.put("groups", List.of("STAMP-02_APP"));

        when(jwt.getClaims()).thenReturn(claims);

        Optional<User> result = userDecoder.fromPrincipal(jwt);

        assertThat(result).isPresent();
        assertThat(result.get().getStamp()).isEqualTo("STAMP-DIRECT");
    }

    @Test
    void shouldHandleEmptyRoles() throws MissingUserInformationException {
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
    void shouldHandleNullSource() throws MissingUserInformationException {
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
    void shouldExtractMultipleRoles() throws MissingUserInformationException {
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
    void shouldHandleInseeGroupsWithNoMatchingSuffix() {
        when(jwtProperties.getInseeGroupClaim()).thenReturn("groups");
        when(jwtProperties.getHieApplicationPrefix()).thenReturn("APP");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("source", "insee");
        claims.put("roles", List.of("USER"));
        claims.put("groups", List.of("GROUP1", "GROUP2"));
        // Groups don't have the _APP suffix

        when(jwt.getClaims()).thenReturn(claims);

        assertThatThrownBy(() -> userDecoder.fromPrincipal(jwt))
            .isInstanceOf(MissingStampException.class);
    }

    @Test
    void shouldHandleNullInseeGroups() {
        when(jwtProperties.getInseeGroupClaim()).thenReturn("groups");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("source", "insee");
        claims.put("roles", List.of("USER"));
        claims.put("groups", null);

        when(jwt.getClaims()).thenReturn(claims);

        assertThatThrownBy(() -> userDecoder.fromPrincipal(jwt))
            .isInstanceOf(MissingStampException.class);
    }

    @Test
    void shouldExtractFirstMatchingGroupFromInseeGroups() throws MissingUserInformationException {
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
        assertThat(result.get().getStamp()).isIn("STAMP-FIRST_APP", "STAMP-SECOND_APP");
    }
}
