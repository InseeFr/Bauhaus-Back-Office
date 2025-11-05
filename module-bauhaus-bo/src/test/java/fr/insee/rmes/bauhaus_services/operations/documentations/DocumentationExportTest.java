package fr.insee.rmes.bauhaus_services.operations.documentations;


import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.code_list.export.CodesListExport;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.utils.FilesUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationExportTest {
    @Mock
    private SeriesUtils seriesUtils;

    @Mock
    private OperationsUtils operationsUtils;

    @Mock
    private IndicatorsUtils indicatorsUtils;

    @Mock
    private ExportUtils exportUtils;

    @Mock
    private CodesListExport codeListService;

    @Mock
    private ParentUtils parentUtils;

    @Mock
    private DocumentationsUtils documentationsUtils;

    @Mock
    private OrganizationsService organizationsService;

    @Mock
    private fr.insee.rmes.domain.port.clientside.OrganisationService organisationService;

    @Mock
    private DocumentsUtils documentsUtils;

    @Test
    void testExportAsZip_success() throws Exception {
        JSONObject document = new JSONObject();
        document.put("url", "file://doc.doc");
        document.put("id", "1");

        when(documentsUtils.getDocumentsUriAndUrlForSims("sims123")).thenReturn(new JSONArray().put(document));
        when(documentsUtils.existsInStorage(any())).thenReturn(false);
        var sims = new JSONObject();
        sims.put("id", "sims123");
        sims.put("labelLg1", "simsLabel");

        var xmlContent = new HashMap<String, String>();
        var xslFile = "xslFile";
        var xmlPattern = "xmlPattern";
        var zip = "zip";
        var objectType = "objectType";

        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, organisationService, documentationsUtils );


        InputStream inputStreamMock = mock(InputStream.class);
        when(exportUtils.exportAsInputStream("simslabel", xmlContent, xslFile, xmlPattern, zip, objectType, FilesUtils.ODT_EXTENSION))
                .thenReturn(inputStreamMock);
        when(inputStreamMock.readAllBytes()).thenReturn(new byte[0]);

        ResponseEntity<Resource> response = documentationExport.exportAsZip(sims, xmlContent, xslFile, xmlPattern, zip, objectType, 50);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("X-Missing-Documents").getFirst()).isEqualTo("1");
    }

    @Test
    void  testExportMetadataReport_Success_WithoutDocuments_Label() throws RmesException {
        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, organisationService, documentationsUtils );

        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = true;
        boolean document = false;
        String goal = Constants.GOAL_COMITE_LABEL;
        String targetType = "someTargetType";

        Resource resource = new ByteArrayResource("Mocked Document Content".getBytes());

        when(documentationsUtils.getDocumentationByIdSims(id)).thenReturn(new JSONObject().put("labelLg1", "labelLg1"));
        when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(new String[]{targetType, "someId"});
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new Documentation());
        when(exportUtils.exportAsODT(any(), any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().body(resource));

        ResponseEntity<Resource> response = documentationExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, goal, 100);
        assertEquals(ResponseEntity.ok().body(resource), response);
    }

    @Test
    void testExportMetadataReport_Failure_UnknownGoal() throws RmesException {
        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, organisationService, documentationsUtils );

        String id = "1234";
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        boolean document = true;
        String goal = "unknownGoal";

        when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(new String[]{"someTargetType", "someId"});
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new Documentation());

        RmesBadRequestException exception = assertThrows(RmesBadRequestException.class,
                () -> documentationExport.exportMetadataReport(id, includeEmptyMas, lg1, lg2, document, goal, 100)
        );

        assertEquals("{\"message\":\"The goal is unknown\"}", exception.getDetails());
    }

    @Test
    void testExportXmlFiles_Success() throws RmesException {
        DocumentationExport documentationExport = new DocumentationExport(50, documentsUtils, exportUtils, seriesUtils, operationsUtils, indicatorsUtils, parentUtils, codeListService, organizationsService, organisationService, documentationsUtils );

        Map<String, String> xmlContent = new HashMap<>();
        boolean includeEmptyMas = true;
        boolean lg1 = true;
        boolean lg2 = false;
        String targetType = "someTargetType";

        when(exportUtils.exportFilesAsResponse(any())).thenReturn(ResponseEntity.ok().body("Mocked File Content"));

        ResponseEntity<Object> response = documentationExport.exportXmlFiles(xmlContent, targetType, includeEmptyMas, lg1, lg2);
        assertEquals(ResponseEntity.ok().body("Mocked File Content"), response);
    }

    @Test
    void testGetXmlContent_WithSeriesUp() throws RmesException {
        // Given
        DocumentationExport documentationExport = new DocumentationExport(
                50,
                documentsUtils,
                exportUtils,
                seriesUtils,
                operationsUtils,
                indicatorsUtils,
                parentUtils,
                codeListService,
                organizationsService,
                organisationService,
                documentationsUtils
        );

        String id = "2179";
        String idDatabase = "s2144";
        Map<String, String> xmlContent = new HashMap<>();

        // Mock parentUtils to return SERIES targetType
        when(parentUtils.getDocumentationTargetTypeAndId(id))
                .thenReturn(new String[]{Constants.SERIES_UP, idDatabase});

        // Mock seriesUtils to return a series
        fr.insee.rmes.model.operations.Series series = createSeriesForTest();
        when(seriesUtils.getSeriesById(idDatabase, fr.insee.rmes.utils.EncodingType.XML))
                .thenReturn(series);

        // Mock organizations
        when(organizationsService.getOrganizations()).thenReturn(new java.util.ArrayList<>());

        // Mock batch organization lookups for creators using OrganisationService from module-domain
        when(organisationService.getOrganisationsMap(java.util.List.of("HIE2004993", "DG75-G401", "DG75-G450")))
                .thenReturn(java.util.Map.of(
                        "HIE2004993", new fr.insee.rmes.domain.model.OrganisationOption("HIE2004993", "Organisation HIE2004993"),
                        "DG75-G401", new fr.insee.rmes.domain.model.OrganisationOption("DG75-G401", "Organisation DG75-G401"),
                        "DG75-G450", new fr.insee.rmes.domain.model.OrganisationOption("DG75-G450", "Organisation DG75-G450")
                ));

        // Mock documentation
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new fr.insee.rmes.model.operations.documentations.Documentation());

        // Mock code lists (empty for simplicity)
        when(codeListService.exportCodesList(any())).thenReturn(
                new fr.insee.rmes.bauhaus_services.code_list.export.ExportedCodesList(
                        "CL_SOURCE_CATEGORY",
                        "Catégorie de source",
                        "Source category",
                        new java.util.ArrayList<>()
                )
        );

        // When
        String targetType = documentationExport.getXmlContent(id, xmlContent);

        // Then
        assertThat(targetType).isEqualTo(Constants.SERIES_UP);
        assertThat(xmlContent).containsKey("seriesFile");

        String seriesFile = xmlContent.get("seriesFile");
        assertThat(seriesFile).startsWith("<Series><id>s2144</id>");
        assertThat(seriesFile).contains("<prefLabelLg1>Comptes nationaux annuels (base 2020)</prefLabelLg1>");
        assertThat(seriesFile).contains("<prefLabelLg2>Annual national accounts (2020 Base)</prefLabelLg2>");
        assertThat(seriesFile).contains("<abstractLg1><p>La comptabilité nationale");
        assertThat(seriesFile).contains("<historyNoteLg1><p>Les origines de la comptabilité nationale");

        // Verify family
        assertThat(seriesFile).contains("<family>");
        assertThat(seriesFile).contains("<id>s30</id>");
        assertThat(seriesFile).contains("<labelLg1>Comptes nationaux</labelLg1>");
        assertThat(seriesFile).contains("<labelLg2>National accounts</labelLg2>");
        assertThat(seriesFile).contains("</family>");

        // Verify type and periodicity
        assertThat(seriesFile).contains("<typeCode>C</typeCode>");
        assertThat(seriesFile).contains("<typeList>CL_SOURCE_CATEGORY</typeList>");
        assertThat(seriesFile).contains("<accrualPeriodicityCode>A</accrualPeriodicityCode>");
        assertThat(seriesFile).contains("<accrualPeriodicityList>CL_FREQ</accrualPeriodicityList>");

        // Verify publishers (nested structure: <publishers><publishers>...</publishers></publishers>)
        assertThat(seriesFile).contains("<publishers><publishers>");
        assertThat(seriesFile).contains("<id>HIE2000007</id>");
        assertThat(seriesFile).contains("<labelLg1>Direction des études et synthèses économiques (DESE)</labelLg1>");
        assertThat(seriesFile).contains("<empty>false</empty>");

        // Verify creators (should contain organization labels instead of stamps)
        assertThat(seriesFile).contains("<creators>");
        assertThat(seriesFile).contains("<creators>Organisation HIE2004993</creators>");
        assertThat(seriesFile).contains("<creators>Organisation DG75-G401</creators>");
        assertThat(seriesFile).contains("<creators>Organisation DG75-G450</creators>");

        // Verify replaces (nested structure: <replaces><replaces>...</replaces></replaces>)
        assertThat(seriesFile).contains("<replaces><replaces>");
        assertThat(seriesFile).contains("<id>s1030</id>");
        assertThat(seriesFile).contains("<type>series</type>");
        assertThat(seriesFile).contains("<labelLg1>Comptes nationaux annuels (base 2014)</labelLg1>");
        assertThat(seriesFile).contains("<labelLg2>Annual national accounts (2014 Base)</labelLg2>");
        assertThat(seriesFile).contains("<empty>false</empty>");

        // Verify idSims and timestamps
        assertThat(seriesFile).contains("<idSims>2179</idSims>");
        assertThat(seriesFile).contains("<created>2023-11-28T09:18:46.114042714</created>");

        assertThat(seriesFile).endsWith("</Series>");
    }

    private fr.insee.rmes.model.operations.Series createSeriesForTest() {
        fr.insee.rmes.model.operations.Series series = new fr.insee.rmes.model.operations.Series();
        series.setId("s2144");
        series.setPrefLabelLg1("Comptes nationaux annuels (base 2020)");
        series.setPrefLabelLg2("Annual national accounts (2020 Base)");
        series.setAbstractLg1("<p>La comptabilité nationale est une représentation globale, détaillée et chiffrée de l'activité économique d'un pays dans un cadre comptable équilibré. Elle décrit les ressources et les emplois à un niveau fin pour chaque type de bien ou de service. L'un des principaux agrégats des comptes nationaux est le produit intérieur brut (PIB) qui reflète l'activité économique interne du pays.</p>\n" +
                "<p>Le 26 mars 2024, les comptes des administrations publiques sont publiées en base 2020.</p>\n" +
                "<p>À partir du 31 mai 2024, les comptes nationaux sont publiés en base 2020.</p>");
        series.setHistoryNoteLg1("<p>Les origines de la comptabilité nationale remontent à l'entre-deux-guerres : l'objectif à l'époque était de construire un indicateur qui donne une évaluation de la richesse produite chaque année et de son évolution. En France, la comptabilité nationale s'est surtout développée dans les années cinquante, pour répondre aux besoins de la planification et des budgets économiques. Adopté en 1996 par le conseil de l'union européenne, le système européen des comptes (SEC95) a le statut de règlement européen. Il s'impose à tous les pays de l'Union. Révisé en 2010, le SEC 2010 est mis en place dans l'Union européenne à partir de 2014.</p>");
        series.setTypeCode("C");
        series.setTypeList("CL_SOURCE_CATEGORY");
        series.setAccrualPeriodicityCode("A");
        series.setAccrualPeriodicityList("CL_FREQ");
        series.setIdSims("2179");
        series.setCreated("2023-11-28T09:18:46.114042714");

        // Set family
        fr.insee.rmes.config.swagger.model.IdLabelTwoLangs family = new fr.insee.rmes.config.swagger.model.IdLabelTwoLangs();
        family.setId("s30");
        family.setLabelLg1("Comptes nationaux");
        family.setLabelLg2("National accounts");
        series.setFamily(family);

        // Set publishers
        fr.insee.rmes.model.links.OperationsLink publisher = new fr.insee.rmes.model.links.OperationsLink(
                "HIE2000007",
                null,
                "Direction des études et synthèses économiques (DESE)",
                null
        );
        series.setPublishers(java.util.List.of(publisher));

        // Set creators
        series.setCreators(java.util.List.of("HIE2004993", "DG75-G401", "DG75-G450"));

        // Set replaces
        fr.insee.rmes.model.links.OperationsLink replaces = new fr.insee.rmes.model.links.OperationsLink(
                "s1030",
                "series",
                "Comptes nationaux annuels (base 2014)",
                "Annual national accounts (2014 Base)"
        );
        series.setReplaces(java.util.List.of(replaces));

        return series;
    }

    @Test
    void testGetXmlContent_WithIndicatorUp() throws RmesException {
        // Given
        DocumentationExport documentationExport = new DocumentationExport(
                50,
                documentsUtils,
                exportUtils,
                seriesUtils,
                operationsUtils,
                indicatorsUtils,
                parentUtils,
                codeListService,
                organizationsService,
                organisationService,
                documentationsUtils
        );

        String id = "2167";
        String idDatabase = "p1723";
        Map<String, String> xmlContent = new HashMap<>();

        // Mock parentUtils to return INDICATOR targetType
        when(parentUtils.getDocumentationTargetTypeAndId(id))
                .thenReturn(new String[]{Constants.INDICATOR_UP, idDatabase});

        // Mock indicatorsUtils to return an indicator
        fr.insee.rmes.model.operations.Indicator indicator = createIndicatorForTest();
        when(indicatorsUtils.getIndicatorById(idDatabase, true))
                .thenReturn(indicator);

        // Mock seriesUtils for the series referenced by the indicator
        fr.insee.rmes.model.operations.Series series = new fr.insee.rmes.model.operations.Series();
        series.setId("s1034");
        series.setPrefLabelLg1("Autres indicateurs");
        series.setPrefLabelLg2("Other indexes");
        series.setCreators(new java.util.ArrayList<>());
        when(seriesUtils.getSeriesById("s1034", fr.insee.rmes.utils.EncodingType.XML))
                .thenReturn(series);

        // Mock organizations
        when(organizationsService.getOrganizations()).thenReturn(new java.util.ArrayList<>());

        // Mock batch organization lookups for creators using OrganisationService from module-domain
        when(organisationService.getOrganisationsMap(java.util.List.of("HIE2004993")))
                .thenReturn(java.util.Map.of(
                        "HIE2004993", new fr.insee.rmes.domain.model.OrganisationOption("HIE2004993", "Organisation HIE2004993")
                ));

        // Mock documentation
        when(documentationsUtils.getFullSimsForXml(id)).thenReturn(new fr.insee.rmes.model.operations.documentations.Documentation());

        // When
        String targetType = documentationExport.getXmlContent(id, xmlContent);

        // Then
        assertThat(targetType).isEqualTo(Constants.INDICATOR_UP);
        assertThat(xmlContent).containsKey("indicatorFile");

        String indicatorFile = xmlContent.get("indicatorFile");
        assertThat(indicatorFile).startsWith("<Indicator><id>p1723</id>");
        assertThat(indicatorFile).contains("<prefLabelLg1>Aide publique au développement (APD) bilatérale</prefLabelLg1>");
        assertThat(indicatorFile).contains("<prefLabelLg2>Bilateral Official Development Assistance (ODA)</prefLabelLg2>");
        assertThat(indicatorFile).contains("<altLabelLg1>ODD 17.i2</altLabelLg1>");

        // Verify abstract
        assertThat(indicatorFile).contains("<abstractLg1>L'Indicateur 17.i2");

        // Verify contributors
        assertThat(indicatorFile).contains("<contributors><contributors>");
        assertThat(indicatorFile).contains("<id>DG75-L002</id>");
        assertThat(indicatorFile).contains("<labelLg1>Administration du comité du Label</labelLg1>");

        // Verify creators (should contain organization label instead of stamp)
        assertThat(indicatorFile).contains("<creators>");
        assertThat(indicatorFile).contains("<creators>Organisation HIE2004993</creators>");

        // Verify wasGeneratedBy
        assertThat(indicatorFile).contains("<wasGeneratedBy><wasGeneratedBy>");
        assertThat(indicatorFile).contains("<id>s1034</id>");
        assertThat(indicatorFile).contains("<labelLg1>Autres indicateurs</labelLg1>");
        assertThat(indicatorFile).contains("<labelLg2>Other indexes</labelLg2>");

        // Verify idSims and timestamps
        assertThat(indicatorFile).contains("<idSims>2167</idSims>");
        assertThat(indicatorFile).contains("<created>2023-12-13T18:42:05.461209844</created>");
        assertThat(indicatorFile).contains("<updated>2025-11-05T10:11:04.793047</updated>");
        assertThat(indicatorFile).contains("<validationState>Unpublished</validationState>");

        assertThat(indicatorFile).endsWith("</Indicator>");
    }

    private fr.insee.rmes.model.operations.Indicator createIndicatorForTest() throws RmesException {
        fr.insee.rmes.model.operations.Indicator indicator = new fr.insee.rmes.model.operations.Indicator();
        indicator.setId("p1723");
        indicator.setPrefLabelLg1("Aide publique au développement (APD) bilatérale");
        indicator.setPrefLabelLg2("Bilateral Official Development Assistance (ODA)");
        indicator.setAltLabelLg1("ODD 17.i2");
        indicator.setAbstractLg1("L'Indicateur 17.i2 **Aide publique au développement (APD)** bilatérale brute comprend deux sous-indicateurs :\n\n" +
                "1. Montant de l'APD bilatérale brute par secteur ou sous-secteur ;\n" +
                "2. Engagements d'APD bilatérale par marqueur.");
        indicator.setAbstractLg2("Indicator 17.i2 **Gross bilateral official development assistance (ODA)** includes two sub-indicators:\n\n" +
                "1. Gross bilateral ODA by sector or sub-sector;\n" +
                "2. Bilateral ODA commitments by marker.");
        indicator.setIdSims("2167");
        indicator.setCreated("2023-12-13T18:42:05.461209844");
        indicator.setUpdated("2025-11-05T10:11:04.793047");
        indicator.setValidationState("Unpublished");

        // Set contributors
        fr.insee.rmes.model.links.OperationsLink contributor = new fr.insee.rmes.model.links.OperationsLink(
                "DG75-L002",
                "organization",
                "Administration du comité du Label",
                null
        );
        indicator.setContributors(java.util.List.of(contributor));

        // Set creators
        indicator.setCreators(java.util.List.of("HIE2004993"));

        // Set wasGeneratedBy
        fr.insee.rmes.model.links.OperationsLink wasGeneratedBy1 = new fr.insee.rmes.model.links.OperationsLink(
                "s1034",
                "series",
                "Autres indicateurs",
                "Other indexes"
        );
        fr.insee.rmes.model.links.OperationsLink wasGeneratedBy2 = new fr.insee.rmes.model.links.OperationsLink(
                "s1034",
                "undefined",
                "Autres indicateurs",
                "Other indexes"
        );
        indicator.setWasGeneratedBy(java.util.List.of(wasGeneratedBy1, wasGeneratedBy2));

        return indicator;
    }
}