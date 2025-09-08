package fr.insee.rmes.config;

import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import java.util.Optional;

import static java.util.Optional.empty;

@Component
public class LogRequestFilter extends AbstractRequestLoggingFilter {

    private static final Logger log = LoggerFactory.getLogger(LogRequestFilter.class);

    private final UserProvider userProvider;

    public LogRequestFilter(@Autowired UserProvider userProvider) {
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
        StringBuilder sb =
                new StringBuilder("From ").append(request.getServerName()).append(" by user ").append(idep)
                        .append(" call ").append(StringUtils.substringBetween(message, "[", "]"));
        if (StringUtils.isNotEmpty(request.getQueryString())) {
            sb.append(request.getQueryString());
        }
        return sb.toString();
    }

    private String getIdUser() {
        Optional<User> currentUser;
        try {
            currentUser = userProvider.findUser();
        } catch (RmesException e) {
            logger.error("while authenticating user", e);
            currentUser = empty();
        }
        return currentUser.map(user -> user.id() + " " + user.getStamp()).orElse("No authentication needed");
    }

}
