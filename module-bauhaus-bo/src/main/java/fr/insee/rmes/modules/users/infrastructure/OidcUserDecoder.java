package fr.insee.rmes.modules.users.infrastructure;

import static java.util.Optional.*;

import com.nimbusds.jose.shaded.gson.JsonElement;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.users.domain.exceptions.EmptyUserInformationException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;

@ServerSideAdaptor
public class OidcUserDecoder implements UserDecoder {

    private static final Logger logger = LoggerFactory.getLogger(OidcUserDecoder.class);
    public static final String LOG_INFO_DEFAULT_STAMP = "User {} uses default stamp";

    private final OrganisationsService organisationService;
    private final JwtProperties jwtProperties;
    private final RoleClaimExtractor roleClaimExtractor;

    public OidcUserDecoder(
            OrganisationsService organisationService,
            JwtProperties jwtProperties,
            RoleClaimExtractor roleClaimExtractor) {
        this.organisationService = organisationService;
        this.jwtProperties = jwtProperties;
        this.roleClaimExtractor = roleClaimExtractor;
    }

    @Override
    public Optional<User> fromPrincipal(Object principal) throws MissingUserInformationException {
        return switch (principal) {
            case String s
            when "anonymousUser".equals(s) -> {
                logger.debug("Anonymous principal, no user decoded");
                yield empty();
            }
            case User user -> {
                logger.debug("Principal is already a user {} with stamps {}", user.id(), user.getStamps());
                yield of(user);
            }
            case Jwt jwt -> {
                var u = of(buildUserFromToken(jwt.getClaims()));
                yield u;
            }
            default -> {
                logger.debug(
                        "Unsupported principal type {}, no user decoded",
                        principal == null ? null : principal.getClass().getName());
                yield empty();
            }
        };
    }

    protected User buildUserFromToken(Map<String, Object> claims) throws MissingUserInformationException {
        if (claims.isEmpty()) {
            throw new EmptyUserInformationException();
        }
        var id = (String) claims.get(jwtProperties.getIdClaim());
        logger.debug("Building user {} from a token with claims {}", id, claims.keySet());
        var stamps = extractStamp(claims, id);

        var source = (String) claims.get(jwtProperties.getSourceClaim());
        var roles = roleClaimExtractor.extractRoles(claims).toList();

        if (stamps.isEmpty()) {
            logger.debug("Current User is {}, without stamp, with roles {} from source {}", id, roles, source);
            return new User(id, roles, Collections.emptySet(), source);
        }

        logger.debug("Current User is {}, {} with roles {} from source {}", id, stamps, roles, source);
        return new User(id, roles, stamps, source);
    }

    private Set<String> extractStamp(Map<String, Object> claims, String userId) {
        logger.debug("Extracting stamp for user {}", userId);

        return ofNullable((String) claims.get(jwtProperties.getStampClaim()))
                .map(stamp -> {
                    logger.debug(
                            "Found stamp in stampClaim '{}' for user {}: {}",
                            jwtProperties.getStampClaim(),
                            userId,
                            stamp);
                    return buildStampsWithAlternateIdentifier(stamp, this::fetchAdmsIdentifier);
                })
                .orElseGet(() -> extractStampFromInseeGroup(claims, userId));
    }

    private Set<String> extractStampFromInseeGroup(Map<String, Object> claims, String userId) {
        logger.debug(
                "No stamp found in stampClaim '{}' for user {}, checking inseeGroupClaim",
                jwtProperties.getStampClaim(),
                userId);

        return extractStampFromInseeGroups(claims.get(jwtProperties.getInseeGroupClaim()))
                .map(hie -> {
                    logger.debug(
                            "Found stamp in inseeGroupClaim '{}' for user {}: {}",
                            jwtProperties.getInseeGroupClaim(),
                            userId,
                            hie);
                    return buildStampsWithAlternateIdentifier(hie, this::fetchDctermsIdentifier);
                })
                .orElseGet(() -> {
                    logger.debug(
                            "No stamp found in inseeGroupClaim '{}' for user {}, using anonymous stamp",
                            jwtProperties.getInseeGroupClaim(),
                            userId);
                    logger.info(LOG_INFO_DEFAULT_STAMP, userId);
                    return Set.of();
                });
    }

    private Set<String> buildStampsWithAlternateIdentifier(String primaryStamp, IdentifierFetcher fetcher) {
        Set<String> stamps = new HashSet<>();
        stamps.add(primaryStamp);
        fetcher.fetch(primaryStamp)
                .ifPresentOrElse(
                        alternate -> {
                            logger.debug("Alternate identifier {} found for stamp {}", alternate, primaryStamp);
                            stamps.add(alternate);
                        },
                        () -> logger.debug("No alternate identifier found for stamp {}", primaryStamp));
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
            logger.debug("Claim '{}' absent from the token", jwtProperties.getInseeGroupClaim());
            return empty();
        }

        String suffix = "_" + jwtProperties.getHieApplicationPrefix();

        return switch (inseeGroups) {
            case List<?> list -> {
                List<String> groups =
                        list.stream().map(this::jsonElementOrElseToString).toList();
                logger.debug("Insee groups {}, looking for the first one ending with '{}'", groups, suffix);
                yield groups.stream()
                        .filter(group -> group.endsWith(suffix))
                        .map(group -> group.substring(0, group.length() - suffix.length()))
                        .findFirst();
            }
            default -> {
                logger.debug(
                        "Claim '{}' is a {}, not a list: no stamp extracted",
                        jwtProperties.getInseeGroupClaim(),
                        inseeGroups.getClass().getName());
                yield empty();
            }
        };
    }

    private String jsonElementOrElseToString(Object element) {
        if (element instanceof JsonElement jsonElement) {
            return jsonElement.getAsString();
        }
        return element.toString();
    }
}
