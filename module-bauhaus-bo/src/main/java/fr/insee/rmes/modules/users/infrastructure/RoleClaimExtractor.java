package fr.insee.rmes.modules.users.infrastructure;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Component
public class RoleClaimExtractor {

    private static final Logger logger = LoggerFactory.getLogger(RoleClaimExtractor.class);

    private final JwtProperties jwtProperties;

    public RoleClaimExtractor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Stream<String> extractRoles(Map<String, Object> claims) {
        logger.debug("Extracting roles from claims using roleClaim '{}'", jwtProperties.getRoleClaim());
        RoleClaim roleClaim = roleClaimFrom(claims);
        ArrayOfRoles arrayOfRoles = roleClaim.arrayOfRoles();
        return arrayOfRoles.stream();
    }

    private RoleClaim roleClaimFrom(Map<String, Object> claims) {
        var rawValue = claims.get(jwtProperties.getRoleClaim());
        logger.debug("Raw value for roleClaim '{}': {} (type: {})", jwtProperties.getRoleClaim(), rawValue, rawValue != null ? rawValue.getClass().getSimpleName() : "null");

        var valueForRoleClaim = switch (rawValue) {
            case null -> {
                logger.debug("RoleClaim is null, returning empty stream");
                yield null;
            }
            case JsonObject objectForRoles -> {
                logger.debug("Processing JsonObject, extracting roles from key '{}'", jwtProperties.getRoleClaimConfig().getRoles());
                yield objectForRoles.getAsJsonArray(jwtProperties.getRoleClaimConfig().getRoles());
            }
            case Map<?, ?> mapForRoles -> {
                logger.debug("Processing Map, extracting roles from key '{}'", jwtProperties.getRoleClaimConfig().getRoles());
                yield mapForRoles.get(jwtProperties.getRoleClaimConfig().getRoles());
            }
            default -> {
                logger.debug("No matching type for roleClaim value, returning null");
                yield null;
            }
        };
        return roleClaimFrom(valueForRoleClaim);
    }

    private RoleClaim roleClaimFrom(Object listOrJsonArray) {
        logger.debug("Converting to RoleClaim: {} (type: {})", listOrJsonArray, listOrJsonArray != null ? listOrJsonArray.getClass().getSimpleName() : "null");

        return switch (listOrJsonArray) {
            case null -> {
                logger.debug("Value is null, returning empty stream");
                yield () -> Stream::empty;
            }
            case JsonArray jsonArray -> {
                logger.debug("Processing JsonArray with {} elements", jsonArray.size());
                yield () -> () -> jsonArrayToStream(jsonArray);
            }
            case List<?> list -> {
                logger.debug("Processing List with {} elements", list.size());
                yield () -> () -> list.stream().map(this::jsonElementOrElseToString);
            }
            default -> {
                logger.debug("Unknown type, returning empty stream");
                yield () -> Stream::empty;
            }
        };
    }

    private String jsonElementOrElseToString(Object element) {
        if (element instanceof JsonElement jsonElement) {
            return jsonElement.getAsString();
        }
        return element.toString();
    }

    private Stream<String> jsonArrayToStream(JsonArray jsonArray) {
        return StreamSupport.stream(Spliterators.spliterator(jsonArray.iterator(), jsonArray.size(), 0), false)
                .map(JsonElement::getAsString);
    }

    private interface RoleClaim {
        ArrayOfRoles arrayOfRoles();
    }

    private interface ArrayOfRoles {
        Stream<String> stream();
    }
}