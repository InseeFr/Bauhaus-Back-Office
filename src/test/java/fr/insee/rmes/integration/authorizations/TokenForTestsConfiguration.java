package fr.insee.rmes.integration.authorizations;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TokenForTestsConfiguration {

    public static final String ROLE_CLAIM = "realm_access";
    public static final String KEY_FOR_ROLES_IN_ROLE_CLAIM = "roles";
    public static final String STAMP_CLAIM = "timbre";
    public static final String ID_CLAIM = "preferred_username";

    public static void configureJwtDecoderMock(JwtDecoder jwtDecoderMock, String idep, String timbre, List<String> roles) {
        when(jwtDecoderMock.decode(anyString())).then(invocation -> Jwt.withTokenValue(invocation.getArgument(0))
                .header("typ", "JWT")
                .header("alg", "none")
                .claim(ID_CLAIM, idep)
                .claim(STAMP_CLAIM, timbre)
                .claim(ROLE_CLAIM, jsonRoles(roles))
                .build());
    }

    private static JSONObject jsonRoles(List<String> roles) {
        var jsonObject = new JSONObject();
        var jsonArray=new JSONArray();
        jsonArray.addAll(roles);
        jsonObject.put(KEY_FOR_ROLES_IN_ROLE_CLAIM, jsonArray);
        return jsonObject;
    }

}
