package fr.insee.rmes.bauhaus_services.datasets;

import static org.mockito.ArgumentMatchers.eq;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetQueries;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class DatasetQueriesTest {

    DatasetQueries datasetQueries = new DatasetQueries(new BauhausLanguagesProperties("fr", "en"));

    @Test
    void shouldCallGetDatasetsQueryWithoutStamp() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("LG1", "\"fr\"");
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees>");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasets.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetQueries.getDatasets("http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees", Set.of());
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetsQueryWithStamp() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("LG1", "\"fr\"");
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees>");
                    put("STAMP", List.of("\"stamp\""));
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasets.ftlh"), eq(map)))
                    .thenReturn("request");
            String query =
                    datasetQueries.getDatasets("http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees", Set.of("stamp"));
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("LG1", "\"fr\"");
                    put("LG2", "\"en\"");
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees>");
                    put("ADMS_GRAPH", "<http://rdf.insee.fr/graphes/adms>");
                    put("ID", "\"1\"");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDataset.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetQueries.getDataset(
                    "1", "http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees", "http://rdf.insee.fr/graphes/adms");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void getDatasetsForSearchShouldExposeAltIdentifierProjection() throws RmesException {
        String query = datasetQueries.getDatasetsForSearch(
                "http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees", "http://rdf.insee.fr/graphes/adms");
        Assertions.assertTrue(
                query.contains("?altIdentifier"),
                "Rendered template must select ?altIdentifier so the search results expose the alternative identifier");
        Assertions.assertTrue(
                query.contains("adms:identifier"),
                "Rendered template must declare the adms:identifier OPTIONAL clause");
        Assertions.assertTrue(
                query.contains("skos:notation"),
                "Rendered template must use skos:notation to fetch the alternative identifier");
        Assertions.assertTrue(
                query.contains("FROM <http://rdf.insee.fr/graphes/adms>"),
                "Rendered template must include the adms graph to resolve altIdentifier triples");
    }

    @Test
    void shouldCallGetDatasetCreatorsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees>");
                    put("ID", "\"1\"");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getDatasetCreators.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetQueries.getDatasetCreators("1", "http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetSpacialResolutionsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees>");
                    put("ID", "\"1\"");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(
                            eq("dataset/"), eq("getDatasetSpacialResolutions.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetQueries.getDatasetSpacialResolutions(
                    "1", "http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetDatasetStatisticalUnitsQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees>");
                    put("ID", "\"1\"");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(
                            eq("dataset/"), eq("getDatasetStatisticalUnits.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetQueries.getDatasetStatisticalUnits(
                    "1", "http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetLastDatasetIdQuery() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_GRAPH", "<http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees>");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(eq("dataset/"), eq("getLastDatasetId.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetQueries.lastDatasetId("http://rdf.insee.fr/graphes/catalogue/jeuDeDonnees");
            Assertions.assertEquals("request", query);
        }
    }

    @Test
    void shouldCallGetContributorsByDatasetUri() throws RmesException {
        try (MockedStatic<FreeMarkerUtils> mockedFactory = Mockito.mockStatic(FreeMarkerUtils.class)) {
            Map<String, Object> map = new HashMap<>() {
                {
                    put("DATASET_URI", "<http://bauhaus/catalogue/jeuDeDonnees/d1000>");
                }
            };
            mockedFactory
                    .when(() -> FreeMarkerUtils.buildRequest(
                            eq("dataset/"), eq("getDatasetsContributorsByUriQuery.ftlh"), eq(map)))
                    .thenReturn("request");
            String query = datasetQueries.getContributorsByDatasetUri("http://bauhaus/catalogue/jeuDeDonnees/d1000");
            Assertions.assertEquals("request", query);
        }
    }
}
