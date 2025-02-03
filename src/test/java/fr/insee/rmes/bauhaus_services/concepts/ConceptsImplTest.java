package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.Stubber;
import fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.model.concepts.CollectionForExportOld;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import fr.insee.rmes.utils.ExportUtils;
import fr.insee.rmes.webservice.concepts.ConceptsCollectionsResources;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptsImplTest {


    @Mock
    ConceptsUtils conceptsUtils;

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    CollectionExportBuilder collectionExport;

    @BeforeAll
    static void initGenericQueries(){
        GenericQueries.setConfig(new ConfigStub());
    }

    @Test
    void shouldGetConceptsList() throws RmesException {
        ConceptsImpl conceptsImpl = new ConceptsImpl(null, null, null, collectionExport, 10);
        Stubber.forRdfService(conceptsImpl).injectRepoGestion(repoGestion);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var concepts = conceptsImpl.getConcepts().stream().toList();

        assertEquals(4, concepts.size());

        assertEquals("3", concepts.get(0).id());
        assertEquals("alabel 1", concepts.get(0).label());
        assertEquals("alatLabel1", concepts.get(0).altLabel());

        assertEquals("2", concepts.get(1).id());
        assertEquals("elabel 1", concepts.get(1).label());
        assertEquals("elatLabel1", concepts.get(1).altLabel());

        assertEquals("4", concepts.get(2).id());
        assertEquals("élabel 1", concepts.get(2).label());
        assertEquals("élatLabel1", concepts.get(2).altLabel());

        assertEquals("1", concepts.get(3).id());
        assertEquals("label 1", concepts.get(3).label());
        assertEquals("latLabel1 || latLabel2", concepts.get(3).altLabel());
    }

    @Test
    void shouldGetConceptsListForAdvancedSearch() throws RmesException {
        ConceptsImpl conceptsImpl = new ConceptsImpl(null, null, null, collectionExport, 10);
        Stubber.forRdfService(conceptsImpl).injectRepoGestion(repoGestion);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel1"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabel", "latLabel2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabel", "elatLabel1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabel", "alatLabel1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabel", "élatLabel1"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var concepts = conceptsImpl.getConceptsSearch().stream().toList();

        assertEquals(4, concepts.size());

        assertEquals("3", concepts.get(0).id());
        assertEquals("alabel 1", concepts.get(0).label());
        assertEquals("alatLabel1", concepts.get(0).altLabel());

        assertEquals("2", concepts.get(1).id());
        assertEquals("elabel 1", concepts.get(1).label());
        assertEquals("elatLabel1", concepts.get(1).altLabel());

        assertEquals("4", concepts.get(2).id());
        assertEquals("élabel 1", concepts.get(2).label());
        assertEquals("élatLabel1", concepts.get(2).altLabel());

        assertEquals("1", concepts.get(3).id());
        assertEquals("label 1", concepts.get(3).label());
        assertEquals("latLabel1 || latLabel2", concepts.get(3).altLabel());
    }

    @Test
    void shouldExportCollection() throws RmesException {
        var collection = new CollectionForExportOld();
        collection.setId("1");
        collection.setPrefLabelLg1("Lg1Collection");
        collection.setPrefLabelLg2("Lg2Collection");

        when(collectionExport.getCollectionDataOld("1")).thenReturn(collection);
        ConceptsImpl conceptsImpl = new ConceptsImpl(null, null, null, collectionExport, 10);

        conceptsImpl.getCollectionExport("1", "application/json");

        verify(collectionExport).exportAsResponse(eq("1Lg1collec"), any(), eq(true), eq(true), eq(true));

    }
    @Test
    void shouldReturnFileName(){
        CollectionForExport collection = new CollectionForExport();
        collection.setId("1");
        collection.setPrefLabelLg1("Lg1Collection");
        collection.setPrefLabelLg2("Lg2Collection");

        ConceptsImpl conceptsImpl = new ConceptsImpl(null, null, null, null, 10);

        assertEquals("1Lg1collec", conceptsImpl.getFileNameForExport(collection, ConceptsCollectionsResources.Language.lg1));
        assertEquals("1Lg2collec", conceptsImpl.getFileNameForExport(collection, ConceptsCollectionsResources.Language.lg2));
    }
    @Test
    void exportConceptTest() throws RmesException, IOException, URISyntaxException {
        // GIVEN
        var idConcept = "c1116";
        ConceptsExportBuilder conceptsExportBuilder = new ConceptsExportBuilder(conceptsUtils, new ExportUtils(200, null));

        Stubber.forRdfService(conceptsExportBuilder).injectRepoGestion(repoGestion);
        ConceptsImpl conceptsImpl = new ConceptsImpl(null, null, conceptsExportBuilder, null, 10);

        JSONObject jsonConcept = new JSONObject("""
                {
                    "valid": "2023-10-18T00:00:00",
                    "creator": "SSM-SDES",
                    "contributor": "DG75-L201",
                    "isValidated": "false",
                    "prefLabelLg1": "Accidents corporels de la circulation",
                    "prefLabelLg2": "Road accidents",
                    "created": "2002-12-23T00:00:00",
                    "modified": "2023-10-19T08:52:59.170187",
                    "id": "c1116",
                    "disseminationStatus": "http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique",
                    "conceptVersion": "2"
                }
                """);
        when(conceptsUtils.getConceptById(idConcept)).thenReturn(jsonConcept);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());
        when(repoGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject(
        """
                {
                    "definitionLg2": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.<\\/p><\\/div>",
                    "definitionLg1": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.<\\/p><\\/div>",
                    "editorialNoteLg1": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Les accidents corporels de la circulation sont définis par l'arrêté du 27 mars 2007 relatif aux conditions d'élaboration des statistiques relatives aux accidents corporels de la circulation.<\\/p><\\/div>",
                    "changeNoteLg1": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Ajout définition courte<\\/p><\\/div>",
                    "editorialNoteLg2": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Accidents involving bodily injury are defined by the order of 27 March 2007 relating to the conditions for compiling statistics on accidents involving bodily injury.<\\/p><\\/div>",
                    "scopeNoteLg2": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.<\\/p><\\/div>",
                    "scopeNoteLg1": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.<\\/p><\\/div>"
                }
                """));

       String expectedXmlForOdtContent = Files.readAllLines(Path.of(ConceptsImplTest.class.getResource("/expectedTestsResult/accidentsCorporelsDeLaCirculation-c1116_content.xml").toURI())).stream()
               .reduce((a,b)->a+"\n"+b).get();

        // WHEN
        ResponseEntity<ByteArrayResource> result = (ResponseEntity<ByteArrayResource>) conceptsImpl.exportConcept(idConcept, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentDisposition().getFilename()).isEqualTo("c1116accid.odt");

        Diff inputsDiffs = DiffBuilder.compare(expectedXmlForOdtContent)
                .withTest(getOdtContent(result.getBody().getByteArray()))
                .ignoreWhitespace()
                .ignoreComments()
                .build();
        assertFalse(inputsDiffs.hasDifferences(), inputsDiffs.fullDescription());
    }

    private String getOdtContent(byte[] byteArray) {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            setoffStreamToEntryContentXML(zipInputStream);
            return new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setoffStreamToEntryContentXML(ZipInputStream zipInputStream) throws IOException {
        ZipEntry zipEntry;
        while((zipEntry= zipInputStream.getNextEntry()) != null &&
                ! "content.xml".equals(zipEntry.getName())) {
        }
    }

}