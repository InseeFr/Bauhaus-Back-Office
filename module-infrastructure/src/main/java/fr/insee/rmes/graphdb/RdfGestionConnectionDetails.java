package fr.insee.rmes.graphdb;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.sesame.gestion")
@Qualifier("rdfGestionConnectionDetails")
public record RdfGestionConnectionDetails(URL urlServer, String repositoryId) implements RdfConnectionDetails {

    @ConstructorBinding
    public RdfGestionConnectionDetails(String sesameServer, String repository) throws MalformedURLException {
        this(URI.create(sesameServer).toURL(), repository);
    }

    @Override
    public String getUrlServer() {
        return urlServer.toString();
    }
}
