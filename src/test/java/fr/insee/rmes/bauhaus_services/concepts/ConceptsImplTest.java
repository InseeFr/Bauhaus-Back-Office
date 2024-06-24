package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.Stubber;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsExportBuilder;
import fr.insee.rmes.bauhaus_services.concepts.concepts.ConceptsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import fr.insee.rmes.utils.ExportUtils;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptsImplTest {

    @Mock
    ConceptsUtils conceptsUtils;

    @Mock
    RepositoryGestion repoGestion;

    @BeforeAll
    static void initGenericQueries(){
        GenericQueries.setConfig(new ConfigStub());
    }
    @Test
    void exportConceptTest() throws RmesException, IOException, URISyntaxException {
        // GIVEN
        var idConcept = "c1116";
        ConceptsExportBuilder conceptsExportBuilder = new ConceptsExportBuilder(conceptsUtils, new ExportUtils(200, null));

        Stubber.forRdfService(conceptsExportBuilder).injectRepoGestion(repoGestion);
        ConceptsImpl conceptsImpl = new ConceptsImpl(null, null, conceptsExportBuilder, null, 200);
        // concept = conceptsExport.getConceptData(id);
        //    conceptsUtils.getConceptById(id) => ConceptsQueries.conceptQuery, ConceptsQueries.altLabel
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
        //     JSONArray links = repoGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id))
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());
        //     JSONObject notes = repoGestion.getResponseAsObject(ConceptsQueries.conceptNotesQuery(id, Integer.parseInt(general.getString(CONCEPT_VERSION))));
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
        // conceptsExport.exportAsResponse
        //      exportUtils.exportAsResponse
        //when(exportUtils.exportAsResponse(anyString(), anyMap(), anyString(), anyString(), anyString(), anyString())).the
       String expectedXmlForOdtContent = Files.readAllLines(Path.of(ConceptsImplTest.class.getResource("/expectedTestsResult/accidentsCorporelsDeLaCirculation-c1116_content.xml").toURI())).stream()
               .reduce((a,b)->a+"\n"+b).get();

        // WHEN
        ResponseEntity<ByteArrayResource> result = (ResponseEntity<ByteArrayResource>) conceptsImpl.exportConcept(idConcept, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // THEN
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(getOdtContent(result.getBody().getByteArray())).isEqualTo(expectedXmlForOdtContent);
    }

    private String getOdtContent(byte[] byteArray) {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while((zipEntry= zipInputStream.getNextEntry()) != null &&
                    ! "content.xml".equals(zipEntry.getName())) {
            }
            return new String(zipInputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void json(){
        var json= """
                {
                  "head" : {
                    "vars" : [
                      "definitionLg1",
                      "definitionLg2",
                      "scopeNoteLg1",
                      "scopeNoteLg2",
                      "editorialNoteLg1",
                      "editorialNoteLg2",
                      "changeNoteLg1",
                      "changeNoteLg2"
                    ]
                  },
                  "results" : {
                    "bindings" : [
                      {
                        "definitionLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
                        },
                        "definitionLg2" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
                        },
                        "scopeNoteLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
                        },
                        "scopeNoteLg2" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
                        },
                        "editorialNoteLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Les accidents corporels de la circulation sont définis par l'arrêté du 27 mars 2007 relatif aux conditions d'élaboration des statistiques relatives aux accidents corporels de la circulation.</p></div>"
                        },
                        "editorialNoteLg2" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Accidents involving bodily injury are defined by the order of 27 March 2007 relating to the conditions for compiling statistics on accidents involving bodily injury.</p></div>"
                        },
                        "changeNoteLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Ajout définition courte</p></div>"
                        }
                      },
                      {
                        "definitionLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
                        },
                        "definitionLg2" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
                        },
                        "scopeNoteLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Est défini comme accident corporel de la circulation tout accident impliquant au moins un véhicule, survenant sur une voie ouverte à la circulation publique, et dans lequel au moins une personne est blessée ou tuée.</p></div>"
                        },
                        "scopeNoteLg2" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>A traffic accident is defined as an accident involving at least one vehicle on a road open to public traffic in which at least one person is injured or killed.</p></div>"
                        },
                        "editorialNoteLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Les accidents corporels de la circulation sont définis par l'arrêté du 27 mars 2007 relatif aux conditions d'élaboration des statistiques relatives aux accidents corporels de la circulation.</p></div>"
                        },
                        "editorialNoteLg2" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Accidents involving bodily injury are defined by the order of 27 March 2007 relating to the conditions for compiling statistics on accidents involving bodily injury.</p></div>"
                        },
                        "changeNoteLg1" : {
                          "datatype" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
                          "type" : "literal",
                          "value" : "<div xmlns=\\"http://www.w3.org/1999/xhtml\\"><p>Ajout définition courte</p></div>"
                        }
                      }
                    ]
                  }
                }
                """;
        System.out.println(RepositoryUtils.sparqlJSONToResultArrayValues(new JSONObject(json)).get(0));
    }

}