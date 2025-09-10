package fr.insee.rmes.config.auth.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.LOG_INFO_DEFAULT_STAMP;
import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.TIMBRE_ANONYME;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenIDConnectSecurityContextTest {

    private static final ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
    private static final String idep = "zzzzzz";
    private static final String timbre = "DR59-SNDI";
    private static final List<String> roles = List.of("role1", "role2", "role3", "role4", "role5");

    private static final List<String> rolesOfAccount = List.of("manage-account",
            "manage-account-links",
            "view-profile");
    private static final JsonArray rolesOfAccountJson = initJsonArray(rolesOfAccount);

    private static final List<String> allowedOrigins = List.of("https://bauhaus.insee",
            "https://bauhaus.test");
    private static final JsonArray allowedOriginsJson = initJsonArray(allowedOrigins);

    private static final JsonArray rolesOfJwt = initJsonArray(roles);

    private static final JsonArray rolesOfRealmAccess = rolesOfJwt;

    private static final Map<String, Object> jwtDecoded = initJwtDecoded();


    private static JsonArray initJsonArray(List<String> elements) {
        var retour = new JsonArray();
        elements.forEach(retour::add);
        return retour;
    }

    private static Map<String, Object> initJwtDecoded() {
        Map<String, Object> retour = new HashMap<>();
        retour.put("sub", "f:fblabbalbalba:ZZZZZZ");
        retour.put("resource_access", simpleJsonObjectOf(
                        "account", simpleJsonObjectOf(
                                "roles", rolesOfAccountJson)
                )
        );
        retour.put("matricule", "0000000000054");
        retour.put("allowed-origins", allowedOriginsJson);
        retour.put("roles", rolesOfJwt);
        retour.put("iss", "https://blabla.fr/a/r/a");
        retour.put("typ", "Bearer");
        retour.put("preferred_username", idep);
        retour.put("given_name", "Fabrice");
        retour.put("sid", "c3000000-0000-0000-0000-000000000");
        retour.put("aud", List.of("account"));
        retour.put("timbre", timbre);
        retour.put("realm_access", simpleJsonObjectOf("roles", rolesOfRealmAccess));
        retour.put("idminefi", "1010101010101");
        retour.put("azp", "blblblblbl-frontend");
        retour.put("auth_time", 1693372846);
        retour.put("scope", "insee-timbre-matricule");
        retour.put("name", "Fabrice Vivonne");
        retour.put("exp", "Wed Aug 30 15,15,33 CEST 2023");
        retour.put("session_state", "c000000-0000-0000-0000-00000000000");
        retour.put("iat", "Wed Aug 30 15,10,33 CEST 2023");
        retour.put("family_name", "Vivonne");
        retour.put("jti", "000000-0000-0000-0000-000000000");
        retour.put("email", "fabrice.vivonne@insee.fr");
        return retour;
    }

    private Jwt springJwt() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("resource_access", Map.of("account", Map.of("account", rolesOfAccount)));
        claims.put("matricule", "0000000000054");
        claims.put("allowed-origins", allowedOrigins);
        claims.put("roles", roles);
        claims.put("typ", "Bearer");
        claims.put("preferred_username", idep);
        claims.put("given_name", "Fabrice");
        claims.put("sid", "c3000000-0000-0000-0000-000000000");
        claims.put("timbre", timbre);
        claims.put("realm_access", Map.of("roles", roles));
        claims.put("idminefi", "1010101010101");
        claims.put("azp", "blblblblbl-frontend");
        claims.put("auth_time", 1693372846);
        claims.put("scope", "insee-timbre-matricule");
        claims.put("name", "Fabrice Vivonne");
        claims.put("session_state", "c000000-0000-0000-0000-00000000000");
        claims.put("family_name", "Vivonne");
        claims.put("email", "fabrice.vivonne@insee.fr");

        Map<String, Object> headers=Map.of(
                "alg", "NONE",
                "typ", "JWT",
                "kid", "boo");
        String subject="f:fblabbalbalba:ZZZZZZ";
        List<String> audiences=List.of("account");
        Instant eat=Instant.MAX;
        Instant iat=now();
        String jti="000000-0000-0000-0000-000000000";
        var auth0Jwt= JWT.create()
                .withSubject(subject)
                .withIssuer("https://blabla.fr/a/r/a")
                .withAudience(audiences.toArray(String[]::new))
                .withExpiresAt(eat)
                .withIssuedAt(iat)
                .withJWTId(jti)
                .withHeader(headers);
        claims.forEach((key, value)->{var unused=switch(value){
            case String valueString -> auth0Jwt.withClaim(key, valueString);
            case Map<?, ?> valueMap -> auth0Jwt.withClaim(key, (Map<String, Object>)valueMap);
            case List<?> valueList -> auth0Jwt.withClaim(key,valueList);
            case Integer valueInt -> auth0Jwt.withClaim(key, valueInt);
            default -> throw new RuntimeException(value+ " cannot be cast to String, Map or List or Integer");
        };});
        String token=auth0Jwt.sign(Algorithm.none());


        return Jwt.withTokenValue(token)
                .claims(c->c.putAll(claims))
                .headers(h->h.putAll(headers))
                .subject(subject)
                .audience(audiences)
                .expiresAt(eat)
                .issuedAt(iat)
                .jti(jti)
                .build();
    }

    private static JsonObject simpleJsonObjectOf(String key, JsonElement value) {
        var retour = new JsonObject();
        retour.add(key, value);
        return retour;
    }

    @BeforeAll
    static void configureLog4j() {
        LoggerContext context = ((LoggerContext) LoggerFactory.getILoggerFactory());
        addAppenderToOidcSecurityContextLogger(createOutputStreamAppender(context), context);
    }

    private static void addAppenderToOidcSecurityContextLogger(Appender<ILoggingEvent> appender, LoggerContext context) {
        var oidcSecurityContextLogger = context.getLogger(OpenIDConnectSecurityContext.class);
        oidcSecurityContextLogger.setLevel(Level.INFO);
        oidcSecurityContextLogger.addAppender(appender);
    }

    private static Appender<ILoggingEvent> createOutputStreamAppender(LoggerContext context) {
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%msg");
        ple.setContext(context);
        ple.start();
        var appender = new OutputStreamAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setEncoder(ple);
        appender.setOutputStream(logOutputStream);
        appender.start();

        return appender;
    }

    @Test
    void testJwt() throws RmesException {
        var oidcContext = new OpenIDConnectSecurityContext("timbre", "realm_access", "preferred_username", false, "roles");

        User user = oidcContext.buildUserFromToken(jwtDecoded);
        assertThat(user.id()).isEqualTo(idep);
        assertThat(user.getStamp()).isEqualTo(timbre);
        assertThat(user.roles()).isEqualTo(roles);
    }

    @Test
    void testJwt_sansTimbre() throws RmesException {
        logOutputStream.reset();

        var oidcContext = new OpenIDConnectSecurityContext("timbr", "realm_access", "preferred_username", false, "roles");

        User user = oidcContext.buildUserFromToken(jwtDecoded);
        assertThat(user.id()).isEqualTo(idep);
        assertThat(user.getStamp()).isEqualTo(TIMBRE_ANONYME);
        assertThat(user.roles()).isEqualTo(roles);
        assertThat(logOutputStream.toString().trim()).hasToString(String.format(LOG_INFO_DEFAULT_STAMP.replace("{}", "%s"), idep));
        logOutputStream.reset();
    }

    @Test
    void testJwtAuthenticationConverter() {
        var oidcContext = new OpenIDConnectSecurityContext("timbre", "realm_access", "preferred_username", false, "roles");
        var jwtAUthenticationConverter = oidcContext.jwtAuthenticationConverter();
        Jwt jwt = springJwt();
        var authenticationToken = jwtAUthenticationConverter.convert(jwt);
        assertTrue(authenticationToken.isAuthenticated());
        assertThat(authenticationToken.getAuthorities()).contains(roles.stream().map(SimpleGrantedAuthority::new).toArray(GrantedAuthority[]::new));
        assertThat(authenticationToken.getName()).isEqualTo(idep);
    }
}