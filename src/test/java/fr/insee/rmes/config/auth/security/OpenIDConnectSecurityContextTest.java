package fr.insee.rmes.config.auth.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.nimbusds.jwt.JWTParser;
import fr.insee.rmes.config.auth.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import java.text.ParseException;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OpenIDConnectSecurityContextTest {

    @Test
    void testJwt() throws ParseException, JSONException {
        var oidcContext = new OpenIDConnectSecurityContext(empty(), "timbre", "realm_access", "preffered_username", false, "roles");

        var roles = new JSONArray("[\"ASI_RMESGNCS\", \"craft_K8S-DEV\", \"gestionnairelocal_Capi3G\", \"si-collecte_K8S-DEV\", \"Administrateur_RMESGNCS\", \"renforts_cigal\", \"Expert_National_ Octopusse\", \"EXPERT_Nautile\", \"ROLE_USER_GDB\", \"observability_K8S-DEV\", \"ASI_GDB\", \"UTILISATEURS_EDL\", \"metadonnees_K8S-DEV\", \"offline_access\", \"namespace-si-collecte_K8S-OVH\", \"uma_authorization\", \"namespace-pogues-eno_K8S-OVH\", \"namespace-advent-of-code-leaderboard_K8S-OVH\", \"maintenance_EEC3\", \"default-roles-agents-insee-interne\", \"Administrateur_CRABE\", \"protools_K8S-DEV\", \"integrateur_Cigal\", \"Api-crabe_Nautile\", \"WRITE_REPO_*_GDB\", \"maintenicien_Capi3G\", \"ROLE_ADMIN_GDB\"]");

        var jwt = JWTParser.parse("***REMOVED***");

        User user = oidcContext.buildUserFromToken(Optional.of(jwt.getJWTClaimsSet().getClaims()));
        assertThat(user.getId()).isEqualTo("xrmfux");
        assertThat(user.getStamp()).isEqualTo("DR59-SNDI");
        assertThat(user.getRoles()).isEqualTo(roles);
    }

    @Test
    void testJwt_sansTimbre() throws ParseException{
        fail();
    }

}