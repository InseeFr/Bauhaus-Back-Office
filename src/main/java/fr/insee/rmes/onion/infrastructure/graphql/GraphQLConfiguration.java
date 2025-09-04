package fr.insee.rmes.onion.infrastructure.graphql;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GraphQLConfiguration implements WebMvcConfigurer  {
    @Override public void addViewControllers(ViewControllerRegistry r) {
        r.addViewController("/playground").setViewName("forward:/playground/index.html");
    }

    @Bean
    public HttpGraphQlClient localGraphQlClient(ServerProperties serverProperties) {
        String contextPath = serverProperties.getServlet() != null && serverProperties.getServlet().getContextPath() != null
                ? serverProperties.getServlet().getContextPath()
                : "";
        String baseUrl = "http://localhost:" + serverProperties.getPort() + contextPath;
        var graphQlWebClient =  WebClient.builder()
                .baseUrl(baseUrl + "/graphql")
                .build();

        return HttpGraphQlClient.builder(graphQlWebClient).build();
    }
}
