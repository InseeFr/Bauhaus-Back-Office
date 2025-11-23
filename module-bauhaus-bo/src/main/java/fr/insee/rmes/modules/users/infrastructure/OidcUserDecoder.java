package fr.insee.rmes.modules.users.infrastructure;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import fr.insee.rmes.modules.users.domain.exceptions.MissingStampException;
import fr.insee.rmes.modules.users.domain.exceptions.EmptyUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.*;

public class OidcUserDecoder implements UserDecoder {
    private static final Logger logger = LoggerFactory.getLogger(OidcUserDecoder.class);
    public static final String LOG_INFO_DEFAULT_STAMP = "User {} uses default stamp";


    private final JwtProperties jwtProperties;

    public OidcUserDecoder(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }


    @Override
    public Optional<User> fromPrincipal(Object principal) throws MissingUserInformationException {
        return "anonymousUser".equals(principal) ?
                empty() :
                of(buildUserFromToken(((Jwt) principal).getClaims()));
    }

    protected User buildUserFromToken(Map<String, Object> claims) throws MissingUserInformationException {
        if (claims.isEmpty()) {
            throw new EmptyUserInformationException();
        }
        var id = (String) claims.get(jwtProperties.getIdClaim());
        var stamp = extractStamp(claims, id);

        if(stamp.isEmpty()){
            throw new MissingStampException(id);
        }

        var source = (String) claims.get(jwtProperties.getSourceClaim());
        var roles = extractRoles(claims).toList();

        logger.debug("Current User is {}, {} with roles {} from source {}", id, stamp, roles, source);
        return new User(id, roles, stamp.get(), source);
    }

    private Optional<String> extractStamp(Map<String, Object> claims, String userId) {
        logger.debug("Extracting stamp for user {}", userId);

        var stamp = ofNullable((String) claims.get(jwtProperties.getStampClaim()));

        if (stamp.isPresent()) {
            logger.debug("Found stamp in stampClaim '{}' for user {}: {}", jwtProperties.getStampClaim(), userId, stamp.get());
            return stamp;
        } else {
            logger.debug("No stamp found in stampClaim '{}' for user {}, checking inseeGroupClaim", jwtProperties.getStampClaim(), userId);

            var inseeGroups = claims.get(jwtProperties.getInseeGroupClaim());
            stamp = extractStampFromInseeGroups(inseeGroups);

            if (stamp.isPresent()) {
                logger.debug("Found stamp in inseeGroupClaim '{}' for user {}: {}", jwtProperties.getInseeGroupClaim(), userId, stamp.get());
                return stamp;
            } else {
                logger.debug("No stamp found in inseeGroupClaim '{}' for user {}, using anonymous stamp", jwtProperties.getInseeGroupClaim(), userId);
                logger.info(LOG_INFO_DEFAULT_STAMP, userId);
                return empty();
            }
        }
    }

    private Optional<String> extractStampFromInseeGroups(Object inseeGroups) {
        if (inseeGroups == null) {
            return empty();
        }

        String suffix = "_" + jwtProperties.getHieApplicationPrefix();

        return switch (inseeGroups) {
            case List<?> list -> list.stream()
                    .map(this::JsonElementOrElseToString)
                    .filter(group -> group.endsWith(suffix))
                    .findFirst();
            default -> empty();
        };
    }

    //TODO dupplicate dans OpenidConnectSecurityContext
    private Stream<String> extractRoles(Map<String, Object> claims) {
        RoleClaim roleClaim = roleClaimFrom(claims);
        ArrayOfRoles arrayOfRoles=roleClaim.arrayOfRoles();
        return arrayOfRoles.stream();
    }

    private RoleClaim roleClaimFrom(Object listOrJsonArray) {
        return switch (listOrJsonArray){
            case JsonArray jsonArray -> () -> () -> jsonArrayToStream(jsonArray);
            case List<?> list -> () -> () -> list.stream().map(this::JsonElementOrElseToString);
            default -> () -> Stream::empty;
        };
    }

    private String JsonElementOrElseToString(Object element) {
        if (element instanceof JsonElement jsonElement){
            return jsonElement.getAsString();
        }
        return element.toString();
    }

    private Stream<String> jsonArrayToStream(JsonArray jsonArray) {
        return StreamSupport.stream(Spliterators.spliterator(jsonArray.iterator(), jsonArray.size(), 0), false)
                .map(JsonElement::getAsString);
    }

    private interface RoleClaim{
        ArrayOfRoles arrayOfRoles();
    }

    private interface ArrayOfRoles{
        Stream<String> stream();
    }
}
