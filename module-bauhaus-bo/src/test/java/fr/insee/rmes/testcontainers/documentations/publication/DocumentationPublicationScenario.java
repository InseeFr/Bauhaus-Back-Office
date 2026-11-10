package fr.insee.rmes.testcontainers.documentations.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.DocumentsStorageProperties;
import fr.insee.rmes.PropertiesKeys;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationPublication;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsPublication;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.modules.operations.msd.DocumentationConfiguration;
import fr.insee.rmes.modules.organisations.OrganisationsProperties;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Publication d'un rapport SIMS et des documents et liens qu'il cite, rejouée sur chaque triplestore
 * (voir {@link DocumentationPublicationGraphDBIntegrationTest} et {@link
 * DocumentationPublicationFusekiIntegrationTest}).
 *
 * <p>Gestion et publication partagent le même dépôt : l'objet publié se distingue par son IRI,
 * réécrite dans la base de publication. Ce qui est vérifié est le graphe où arrivent le rapport (son
 * propre graphe) et le document et le lien (graphe des documents) : sous Fuseki, un triplet relu sans
 * graphe nommé n'en porte aucun, et la publication partait dans le graphe par défaut sans erreur.
 *
 * <p>Seul le stockage des fichiers est simulé : le fichier du document existe et sa copie ne fait rien.
 */
interface DocumentationPublicationScenario {

    /** Les graphes de {@link GraphsPropertiesStub}. */
    String SIMS_GRAPH = "http://rdf.insee.fr/graphes/qualite/rapport/9601";

    String DOCUMENTS_GRAPH = "http://rdf.insee.fr/graphes/qualite/documents";

    String ORGANISATIONS_GRAPH = "http://rdf.insee.fr/graphes/organisations";
    String ORGANISATIONS_INSEE_GRAPH = "http://rdf.insee.fr/graphes/organisations/insee";
    String TERRITORIES_GRAPH = "http://rdf.insee.fr/graphes/qualite/territoires";

    String GESTION_BASE = "http://bauhaus/";
    String PUBLICATION_BASE = "http://id.insee.fr/";
    String WEB4G = "http://web4g";
    String SIMS = GESTION_BASE + "qualite/rapport/9601";
    String TEXT = SIMS + "/S.12.1/fr";
    String DOCUMENT = GESTION_BASE + "documents/document/9601";
    String LINK = GESTION_BASE + "documents/page/9602";
    String PUBLISHED_SIMS = PUBLICATION_BASE + "qualite/rapport/9601";
    String PUBLISHED_TEXT = PUBLISHED_SIMS + "/S.12.1/fr";
    String PUBLISHED_DOCUMENT = PUBLICATION_BASE + "documents/document/9601";
    String PUBLISHED_LINK = PUBLICATION_BASE + "documents/page/9602";
    String ORGANISATION = GESTION_BASE + "organisations/insee/DG75-F9601";
    String TERRITORY = GESTION_BASE + "qualite/territoire/t9601";
    String PUBLISHED_ORGANISATION = PUBLICATION_BASE + "organisations/insee/DG75-F9601";
    String PUBLISHED_TERRITORY = PUBLICATION_BASE + "qualite/territoire/t9601";

    RdfConnectionDetails connectionDetails();

    RepositoryUtils repositoryUtils();

    /**
     * Le conteneur n'est vidé qu'entre deux classes : chaque test repart d'un rapport absent. Le graphe
     * des documents peut porter les fixtures d'autres classes, seuls les sujets du test y sont effacés.
     */
    @BeforeEach
    default void removeTheSimsAndItsDocuments() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        Map<String, String> baseUris = Map.of(
                PropertiesKeys.DOCUMENTATIONS_BASE_URI, "qualite/rapport",
                PropertiesKeys.DOCUMENTS_BASE_URI, "documents/document",
                PropertiesKeys.LINKS_BASE_URI, "documents/page");
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder(PUBLICATION_BASE, GESTION_BASE, name -> Optional.ofNullable(baseUris.get(name))));
        StringBuilder update = new StringBuilder("DROP SILENT GRAPH <%s>".formatted(SIMS_GRAPH));
        for (String subject : new String[] {DOCUMENT, LINK, PUBLISHED_DOCUMENT, PUBLISHED_LINK}) {
            update.append(" ; DELETE WHERE { GRAPH <%s> { <%s> ?p ?o } }".formatted(DOCUMENTS_GRAPH, subject));
        }
        for (String subject : new String[] {ORGANISATION, TERRITORY, PUBLISHED_ORGANISATION, PUBLISHED_TERRITORY}) {
            update.append(" ; DELETE WHERE { GRAPH ?graph { <%s> ?p ?o } }".formatted(subject));
        }
        gestion().executeUpdate(update.toString());
    }

    @Test
    default void publishes_the_sims_into_its_own_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        insertTheSimsWithItsDocumentAndLink(gestion);

        documentationPublication(gestion).publishSims("9601");

        assertThat(publishedLabels(gestion, SIMS_GRAPH, PUBLISHED_SIMS)).isEqualTo("Rapport qualité Emploi");
        assertThat(gestion.getResponseAsBoolean(
                        "ASK { GRAPH <%s> { <%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"Texte de la rubrique\" } }"
                                .formatted(SIMS_GRAPH, PUBLISHED_TEXT)))
                .isTrue();
    }

    @Test
    default void publishes_the_document_and_the_link_into_the_documents_graph() throws RmesException {
        RepositoryGestion gestion = gestion();
        insertTheSimsWithItsDocumentAndLink(gestion);

        documentationPublication(gestion).publishSims("9601");

        assertThat(publishedLabels(gestion, DOCUMENTS_GRAPH, PUBLISHED_DOCUMENT))
                .isEqualTo("Note méthodologique");
        assertThat(gestion.getResponseAsBoolean("ASK { GRAPH <%s> { <%s> <http://schema.org/url> <%s> } }"
                        .formatted(DOCUMENTS_GRAPH, PUBLISHED_DOCUMENT, WEB4G + "/note-9601.pdf")))
                .isTrue();
        assertThat(publishedLabels(gestion, DOCUMENTS_GRAPH, PUBLISHED_LINK)).isEqualTo("Page de l'enquête");
    }

    @Test
    default void republishing_the_sims_replaces_the_previous_publication_of_its_document_and_link()
            throws RmesException {
        RepositoryGestion gestion = gestion();
        DocumentationPublication documentationPublication = documentationPublication(gestion);
        insertTheSimsWithItsDocumentAndLink(gestion);
        documentationPublication.publishSims("9601");

        // gestion et publication partagent le graphe du rapport : sa publication l'a remplacé
        gestion.executeUpdate(simsLabelled("Rapport qualité Emploi"));
        relabel(gestion, DOCUMENT, "Note méthodologique 2026");
        relabel(gestion, LINK, "Page de l'enquête 2026");
        documentationPublication.publishSims("9601");

        assertThat(publishedLabels(gestion, DOCUMENTS_GRAPH, PUBLISHED_DOCUMENT))
                .isEqualTo("Note méthodologique 2026");
        assertThat(publishedLabels(gestion, DOCUMENTS_GRAPH, PUBLISHED_LINK)).isEqualTo("Page de l'enquête 2026");
    }

    /**
     * L'organisation citée vit dans deux graphes (organisations et organisations de l'Insee), le
     * territoire dans celui des territoires du SIMS : chaque triplet publié reste dans son graphe.
     */
    @Test
    default void publishes_the_cited_organisation_and_territory_into_their_own_graphs() throws RmesException {
        RepositoryGestion gestion = gestion();
        insertTheSimsWithItsDocumentAndLink(gestion);
        gestion.executeUpdate(organisationAndTerritoryCitedByTheSims());

        documentationPublication(gestion).publishSims("9601");

        assertThat(gestion.getResponseAsBoolean("""
                        ASK {
                          GRAPH <%s> { <%s> <http://www.w3.org/2000/01/rdf-schema#label> "Direction des statistiques d'entreprises"@fr }
                          GRAPH <%s> { <%2$s> <http://rdf.insee.fr/def/base#identifiant> "DG75-F9601" }
                          GRAPH <%s> { <%s> <http://www.w3.org/2004/02/skos/core#prefLabel> "France hors Mayotte"@fr }
                        }
                        """.formatted(
                                ORGANISATIONS_GRAPH,
                                PUBLISHED_ORGANISATION,
                                ORGANISATIONS_INSEE_GRAPH,
                                TERRITORIES_GRAPH,
                                PUBLISHED_TERRITORY)))
                .isTrue();
    }

    private static String organisationAndTerritoryCitedByTheSims() {
        return """
                INSERT DATA {
                  GRAPH <%s> { <%s/S.3.7> <http://bauhaus/qualite/simsv2fr/attribut/S.3.7> <%s>, <%s> . }
                  GRAPH <%s> { <%3$s> <http://www.w3.org/2000/01/rdf-schema#label> "Direction des statistiques d'entreprises"@fr . }
                  GRAPH <%s> { <%3$s> <http://rdf.insee.fr/def/base#identifiant> "DG75-F9601" . }
                  GRAPH <%s> { <%4$s> <http://www.w3.org/2004/02/skos/core#prefLabel> "France hors Mayotte"@fr . }
                }
                """.formatted(
                        SIMS_GRAPH,
                        SIMS,
                        ORGANISATION,
                        TERRITORY,
                        ORGANISATIONS_GRAPH,
                        ORGANISATIONS_INSEE_GRAPH,
                        TERRITORIES_GRAPH);
    }

    private RepositoryGestion gestion() {
        return new RepositoryGestion(connectionDetails(), repositoryUtils());
    }

    private DocumentationPublication documentationPublication(RepositoryGestion gestion) {
        RepositoryPublication publication = new RepositoryPublication(
                connectionDetails().getUrlServer(), connectionDetails().repositoryId(), repositoryUtils());
        PublicationUtils publicationUtils = new PublicationUtils(GESTION_BASE, PUBLICATION_BASE, gestion, publication);
        FilesOperations files = mock(FilesOperations.class);
        when(files.exists(any(Document.class))).thenReturn(true);
        StorageProperties storage = new StorageProperties("/gestion", "/publication");
        OperationDocumentsQueries queries = new OperationDocumentsQueries(
                new BauhausUriProperties(
                        GESTION_BASE, "documents/page", "codes", "documents/document", "produits/indicateur"),
                new BauhausLanguagesProperties("fr", "en"),
                GraphsPropertiesStub.stub());
        DocumentsUtils documentsUtils =
                new DocumentsUtils(gestion, null, publication, publicationUtils, files, storage, queries);
        DocumentsPublication documentsPublication = new DocumentsPublication(
                gestion,
                null,
                publication,
                publicationUtils,
                documentsUtils,
                files,
                storage,
                new DocumentsStorageProperties("/gestion", WEB4G),
                queries);
        return new DocumentationPublication(
                gestion,
                publication,
                publicationUtils,
                documentsPublication,
                new DocumentationConfiguration(
                        new DocumentationConfiguration.Geographie("territoire"),
                        "Rapport qualité :",
                        "Quality report:"),
                new OrganisationsProperties("organisations"));
    }

    private static void insertTheSimsWithItsDocumentAndLink(RepositoryGestion gestion) throws RmesException {
        gestion.executeUpdate(simsLabelled("Rapport qualité Emploi"));
        gestion.executeUpdate(documentLabelled(DOCUMENT, "Note méthodologique", "file:///gestion/note-9601.pdf"));
        gestion.executeUpdate(documentLabelled(LINK, "Page de l'enquête", "https://www.insee.fr/enquete"));
    }

    private static String simsLabelled(String label) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://www.w3.org/ns/sdmx-mm#MetadataReport> ;
                    <http://www.w3.org/2000/01/rdf-schema#label> "%s"@fr ;
                    <http://rdf.insee.fr/def/base#validationState> "Unpublished" .
                  <%s> a <http://purl.org/dc/dcmitype/Text> ;
                    <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> "Texte de la rubrique" ;
                    <http://rdf.insee.fr/def/base#additionalMaterial> ( <%s> <%s> ) .
                } }
                """.formatted(SIMS_GRAPH, SIMS, label, TEXT, DOCUMENT, LINK);
    }

    private static String documentLabelled(String subject, String label, String url) {
        return """
                INSERT DATA { GRAPH <%s> {
                  <%s> a <http://xmlns.com/foaf/0.1/Document> ;
                    <http://schema.org/url> <%s> ;
                    <http://www.w3.org/2000/01/rdf-schema#label> "%s"@fr .
                } }
                """.formatted(DOCUMENTS_GRAPH, subject, url, label);
    }

    private static void relabel(RepositoryGestion gestion, String subject, String label) throws RmesException {
        gestion.executeUpdate("""
                DELETE WHERE { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2000/01/rdf-schema#label> ?label } } ;
                INSERT DATA { GRAPH <%1$s> { <%2$s> <http://www.w3.org/2000/01/rdf-schema#label> "%3$s"@fr } }
                """.formatted(DOCUMENTS_GRAPH, subject, label));
    }

    /** Les libellés de l'objet publié lus dans le graphe donné, séparés par « | ». */
    private static String publishedLabels(RepositoryGestion gestion, String graph, String published)
            throws RmesException {
        return gestion.getResponseAsObject("""
                        SELECT (GROUP_CONCAT(?label; separator="|") AS ?labels) WHERE {
                          GRAPH <%s> { <%s> <http://www.w3.org/2000/01/rdf-schema#label> ?label }
                        }
                        """.formatted(graph, published)).optString("labels");
    }
}
