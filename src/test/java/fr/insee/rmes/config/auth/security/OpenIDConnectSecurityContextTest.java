package fr.insee.rmes.config.auth.security;

import com.nimbusds.jwt.JWTParser;
import fr.insee.rmes.config.auth.user.User;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.Optional;

import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.LOG_INFO_DEFAULT_STAMP;
import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.TIMBRE_ANONYME;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

class OpenIDConnectSecurityContextTest {

    private static ByteArrayOutputStream logOutputStream=new ByteArrayOutputStream();

    @BeforeAll
    static void configureLog4j(){

        final Configuration config = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
        final Appender appender = createOutputStreamAppender(config);
        addAppenderEverywhere(config, appender);
        forceInfoForRootLogger(config);
    }

    private static void addAppenderEverywhere(Configuration config, Appender appender) {
        config.addAppender(appender);
        config.getRootLogger().addAppender(appender, Level.INFO, null);
        for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.addAppender(appender, Level.INFO, null);
        }
    }

    @NotNull
    private static Appender createOutputStreamAppender(Configuration config) {
        final PatternLayout layout = PatternLayout.createDefaultLayout(config);
        final Appender appender = OutputStreamAppender.createAppender(layout, null, logOutputStream, "StdoutTest", false, true);
        appender.start();
        return appender;
    }

    private static void forceInfoForRootLogger(Configuration config) {
        config.getRootLogger().setLevel(Level.INFO);
        config.getRootLogger().setAdditive(true);
    }

    @Test
    void testJwt() throws ParseException, JSONException {
        var oidcContext = new OpenIDConnectSecurityContext(empty(), "timbre", "realm_access", "preferred_username", false, "roles");

        var roles = new JSONArray("[\"ASI_RMESGNCS\", \"craft_K8S-DEV\", \"gestionnairelocal_Capi3G\", \"si-collecte_K8S-DEV\", \"Administrateur_RMESGNCS\", \"renforts_cigal\", \"Expert_National_ Octopusse\", \"EXPERT_Nautile\", \"ROLE_USER_GDB\", \"observability_K8S-DEV\", \"ASI_GDB\", \"UTILISATEURS_EDL\", \"metadonnees_K8S-DEV\", \"offline_access\", \"namespace-si-collecte_K8S-OVH\", \"uma_authorization\", \"namespace-pogues-eno_K8S-OVH\", \"namespace-advent-of-code-leaderboard_K8S-OVH\", \"maintenance_EEC3\", \"default-roles-agents-insee-interne\", \"Administrateur_CRABE\", \"protools_K8S-DEV\", \"integrateur_Cigal\", \"Api-crabe_Nautile\", \"WRITE_REPO_*_GDB\", \"maintenicien_Capi3G\", \"ROLE_ADMIN_GDB\"]");

        var jwt = JWTParser.parse("***REMOVED***");

        User user = oidcContext.buildUserFromToken(Optional.of(jwt.getJWTClaimsSet().getClaims()));
        assertThat(user.getId()).isEqualTo("xrmfux");
        assertThat(user.getStamp()).isEqualTo("DR59-SNDI");
        assertThat(user.getRoles()).isEqualTo(roles);
    }

    @Test
    void testJwt_sansTimbre() throws ParseException, JSONException {
        logOutputStream.reset();

        var oidcContext = new OpenIDConnectSecurityContext(empty(), "timbr", "realm_access", "preferred_username", false, "roles");

        var roles = new JSONArray("[\"ASI_RMESGNCS\", \"craft_K8S-DEV\", \"gestionnairelocal_Capi3G\", \"si-collecte_K8S-DEV\", \"Administrateur_RMESGNCS\", \"renforts_cigal\", \"Expert_National_ Octopusse\", \"EXPERT_Nautile\", \"ROLE_USER_GDB\", \"observability_K8S-DEV\", \"ASI_GDB\", \"UTILISATEURS_EDL\", \"metadonnees_K8S-DEV\", \"offline_access\", \"namespace-si-collecte_K8S-OVH\", \"uma_authorization\", \"namespace-pogues-eno_K8S-OVH\", \"namespace-advent-of-code-leaderboard_K8S-OVH\", \"maintenance_EEC3\", \"default-roles-agents-insee-interne\", \"Administrateur_CRABE\", \"protools_K8S-DEV\", \"integrateur_Cigal\", \"Api-crabe_Nautile\", \"WRITE_REPO_*_GDB\", \"maintenicien_Capi3G\", \"ROLE_ADMIN_GDB\"]");

        var jwtSansTimbre = JWTParser.parse("***REMOVED***");

        User user = oidcContext.buildUserFromToken(Optional.of(jwtSansTimbre.getJWTClaimsSet().getClaims()));
        assertThat(user.getId()).isEqualTo("xrmfux");
        assertThat(user.getStamp()).isEqualTo(TIMBRE_ANONYME);
        assertThat(user.getRoles()).isEqualTo(roles);
        assertThat(logOutputStream.toString().trim()).hasToString(String.format(LOG_INFO_DEFAULT_STAMP.replace("{}", "%s"), "xrmfux"));
        logOutputStream.reset();
    }
}