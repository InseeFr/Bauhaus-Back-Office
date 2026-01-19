package fr.insee.rmes.modules.users.infrastructure;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.domain.port.clientside.OrganisationService;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.exceptions.MissingStampException;
import fr.insee.rmes.modules.users.domain.exceptions.EmptyUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.*;

@ServerSideAdaptor
public class OidcUserDecoder implements UserDecoder {
    private static final Logger logger = LoggerFactory.getLogger(OidcUserDecoder.class);
    public static final String LOG_INFO_DEFAULT_STAMP = "User {} uses default stamp";

    private final OrganisationsService organisationService;
    private final JwtProperties jwtProperties;

    public OidcUserDecoder(OrganisationsService organisationService, JwtProperties jwtProperties) {
        this.organisationService = organisationService;
        this.jwtProperties = jwtProperties;
    }


    @Override
    public Optional<User> fromPrincipal(Object principal) throws MissingUserInformationException {
        return switch (principal) {
            case String s when "anonymousUser".equals(s) -> empty();
            case User user ->  of(user);
            case Jwt jwt -> {
                var u = of(buildUserFromToken(jwt.getClaims()));
                yield u;
            }
            default -> empty();
        };
    }

    protected User buildUserFromToken(Map<String, Object> claims) throws MissingUserInformationException {
        if (claims.isEmpty()) {
            throw new EmptyUserInformationException();
        }
        var id = (String) claims.get(jwtProperties.getIdClaim());
        var stamps = extractStamp(claims, id);

        var source = (String) claims.get(jwtProperties.getSourceClaim());
        var roles = extractRoles(claims).toList();

        if(stamps.isEmpty()){
            return new User(id, roles, Collections.emptySet(), source);
        }

        logger.debug("Current User is {}, {} with roles {} from source {}", id, stamps, roles, source);
        return new User(id, roles, stamps, source);
    }

    private Set<String> extractStamp(Map<String, Object> claims, String userId) {
        logger.debug("Extracting stamp for user {}", userId);

        return ofNullable((String) claims.get(jwtProperties.getStampClaim()))
                .map(stamp -> {
                    logger.debug("Found stamp in stampClaim '{}' for user {}: {}", jwtProperties.getStampClaim(), userId, stamp);
                    return buildStampsWithAlternateIdentifier(stamp, this::fetchAdmsIdentifier);
                })
                .orElseGet(() -> extractStampFromInseeGroup(claims, userId));
    }

    private Set<String> extractStampFromInseeGroup(Map<String, Object> claims, String userId) {
        logger.debug("No stamp found in stampClaim '{}' for user {}, checking inseeGroupClaim", jwtProperties.getStampClaim(), userId);

        return extractStampFromInseeGroups(claims.get(jwtProperties.getInseeGroupClaim()))
                .map(hie -> {
                    logger.debug("Found stamp in inseeGroupClaim '{}' for user {}: {}", jwtProperties.getInseeGroupClaim(), userId, hie);
                    return buildStampsWithAlternateIdentifier(hie, this::fetchDctermsIdentifier);
                })
                .orElseGet(() -> {
                    logger.debug("No stamp found in inseeGroupClaim '{}' for user {}, using anonymous stamp", jwtProperties.getInseeGroupClaim(), userId);
                    logger.info(LOG_INFO_DEFAULT_STAMP, userId);
                    return Set.of();
                });
    }

    private Set<String> buildStampsWithAlternateIdentifier(String primaryStamp, IdentifierFetcher fetcher) {
        Set<String> stamps = new HashSet<>();
        stamps.add(primaryStamp);
        fetcher.fetch(primaryStamp).ifPresent(stamps::add);
        return stamps;
    }

    private Optional<String> fetchAdmsIdentifier(String stamp) {
        try {
            return organisationService.getAdmsIdentifier(stamp);
        } catch (OrganisationFetchException e) {
            logger.debug("Impossible to fetch the adms:identifier for stamp {}", stamp);
            return empty();
        }
    }

    private Optional<String> fetchDctermsIdentifier(String hie) {
        try {
            return organisationService.getDctermsIdentifier(hie);
        } catch (OrganisationFetchException e) {
            logger.debug("Impossible to fetch the dcterms:identifier for hie {}", hie);
            return empty();
        }
    }

    @FunctionalInterface
    private interface IdentifierFetcher {
        Optional<String> fetch(String identifier);
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
        Object roleClaimValue = claims.get(jwtProperties.getRoleClaim());
        RoleClaim roleClaim = roleClaimFrom(roleClaimValue);
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
