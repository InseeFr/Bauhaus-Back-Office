package fr.insee.rmes.modules.commons.configuration;

import static java.util.Optional.empty;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Component
public class LogRequestFilter extends AbstractRequestLoggingFilter {

    private static final Logger log = LoggerFactory.getLogger(LogRequestFilter.class);

    private final UserProvider userProvider;

    public LogRequestFilter(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        String logRequest = this.getFormatLogRequest(request, message, getIdUser());
        log.info("START {}", logRequest);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        String logRequest = this.getFormatLogRequest(request, message, getIdUser());
        log.info("END {}", logRequest);
    }

    private String getFormatLogRequest(HttpServletRequest request, String message, String idep) {
        String queryString = StringUtils.isNotEmpty(request.getQueryString()) ? request.getQueryString() : "";
        return String.format(
                "From %s by user %s call %s%s",
                request.getServerName(), idep, StringUtils.substringBetween(message, "[", "]"), queryString);
    }

    private String getIdUser() {
        Optional<User> currentUser;
        try {
            currentUser = userProvider.findUser();
        } catch (RmesException | MissingUserInformationException e) {
            logger.error("while authenticating user", e);
            currentUser = empty();
        }
        return currentUser.map(user -> user.id() + " " + user.getStamps()).orElse("No authentication needed");
    }
}
