package fr.insee.rmes.modules.operation.series.infrastructure;

import fr.insee.rmes.PropertiesKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Translates a publication-prefixed series IRI (e.g. {@code http://id.insee.fr/...}) into its
 * management-base equivalent (e.g. {@code http://bauhaus/...}).
 * <p>
 * The Group built by the DDI init stores the <em>publication</em> IRI (the canonical identifier the
 * front uses to resolve {@code GET /ddi/operation/{id}/studyUnit}), whereas the security path queries
 * the management repository, whose subjects carry the {@code gestion} prefix. This rewriter keeps that
 * prefix knowledge confined to the management infrastructure.
 */
@Component
public class PublicationToGestionIriRewriter {

    private final String publicationBaseUri;
    private final String gestionBaseUri;

    public PublicationToGestionIriRewriter(
            @Value("${" + PropertiesKeys.BASE_URI_PUBLICATION + "}") String publicationBaseUri,
            @Value("${" + PropertiesKeys.BASE_URI_GESTION + "}") String gestionBaseUri) {
        this.publicationBaseUri = publicationBaseUri;
        this.gestionBaseUri = gestionBaseUri;
    }

    /**
     * @return the management-base equivalent of {@code iri} if it starts with the publication base URI;
     *         otherwise {@code iri} unchanged (null/blank are returned as-is).
     */
    public String toGestion(String iri) {
        if (iri == null || iri.isBlank()) {
            return iri;
        }
        if (iri.startsWith(publicationBaseUri)) {
            return gestionBaseUri + iri.substring(publicationBaseUri.length());
        }
        return iri;
    }
}
