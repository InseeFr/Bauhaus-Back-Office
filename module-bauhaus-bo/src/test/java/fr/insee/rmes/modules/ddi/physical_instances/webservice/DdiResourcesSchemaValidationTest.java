package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ValueType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.Ddi4SchemaRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.DomainDdi4SchemaService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema.ClasspathDdi4SchemaRepository;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.schema.NetworkntDdi4SchemaValidator;
import fr.insee.rmes.modules.ddi.physical_instances.webservice.response.ValidationResponse;
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * {@code POST /ddi/validate} valide le DDI 4 tel qu'il circule avec le front, c'est-à-dire
 * directement sous l'enveloppe du {@code ddi-schema.json} : {@code topLevelReferences} +
 * {@code items}. Aucune traduction n'a lieu à la validation — si le payload ne valide pas, c'est
 * que le contrat de fil a dérivé.
 * <p>
 * Ces tests jouent le vrai schéma (pas de schéma bouchon, contrairement à
 * {@link DdiResourcesValidationTest}).
 */
@ExtendWith(MockitoExtension.class)
class DdiResourcesSchemaValidationTest {

    @Mock
    private DDIService ddiService;

    @Mock
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @Mock
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    @Mock
    private DDIItemConvertService ddiItemConvertService;

    @Mock
    private UserProvider userProvider;

    @Mock
    private RbacFetcher rbacFetcher;

    @Mock
    private UriUtils uriUtils;

    private DdiResources ddiResources;

    /**
     * L'ancienne sérialisation Colectica, groupée par type. Conservée pour verrouiller son
     * rejet : elle ne doit plus circuler.
     */
    private static final String COLECTICA_FRAGMENT_SET =
            """
            {
              "$schema": "ddi:4.0",
              "TopLevelReference": [
                {"$type": "PhysicalInstance", "URN": "urn:ddi:fr.insee:pi-1:1", "Agency": "fr.insee", "ID": "pi-1", "Version": "1"}
              ],
              "PhysicalInstance": [
                {
                  "$type": "PhysicalInstance",
                  "VersionDate": {"DateTime": "2026-08-06T13:28:10.283854228+01:00"},
                  "URN": "urn:ddi:fr.insee:pi-1:1", "Agency": "fr.insee", "ID": "pi-1", "Version": "1",
                  "Citation": {"Title": [{"@language": "fr-FR", "@value": "20260804"}]},
                  "DataRelationshipReference": [
                    {"$type": "DataRelationship", "URN": "urn:ddi:fr.insee:dr-1:1", "Agency": "fr.insee", "ID": "dr-1", "Version": "1"}
                  ]
                }
              ],
              "DataRelationship": [
                {
                  "$type": "DataRelationship",
                  "VersionDate": {"DateTime": "2026-08-06T13:28:10.283854228+01:00"},
                  "URN": "urn:ddi:fr.insee:dr-1:1", "Agency": "fr.insee", "ID": "dr-1", "Version": "1",
                  "Label": [{"@language": "fr-FR", "@value": "Structure"}],
                  "LogicalRecord": [
                    {
                      "$type": "LogicalRecordType",
                      "URN": "urn:ddi:fr.insee:lr-1:1", "Agency": "fr.insee", "ID": "lr-1", "Version": "1",
                      "Label": [{"@language": "fr-FR", "@value": "Enregistrement logique"}],
                      "VariablesInRecord": {
                        "VariableUsedReference": [
                          {"$type": "Variable", "URN": "urn:ddi:fr.insee:var-1:1", "Agency": "fr.insee", "ID": "var-1", "Version": "1"}
                        ]
                      }
                    }
                  ]
                }
              ],
              "Variable": [
                {
                  "$type": "Variable",
                  "VersionDate": {"DateTime": "2026-08-06T10:27:45.371360683+01:00"},
                  "URN": "urn:ddi:fr.insee:var-1:1", "Agency": "fr.insee", "ID": "var-1", "Version": "1",
                  "VariableName": [{"@language": "fr-FR", "@value": "copy"}],
                  "Label": [{"@language": "fr-FR", "@value": "cocpy"}],
                  "VariableRepresentation": {
                    "CodeRepresentation": {
                      "$type": "CodeRepresentationBaseType",
                      "BlankIsMissingValue": false,
                      "CodeListReference": {"$type": "CodeList", "URN": "urn:ddi:fr.insee:cl-1:1", "Agency": "fr.insee", "ID": "cl-1", "Version": "1"}
                    }
                  }
                }
              ],
              "CodeList": [
                {
                  "$type": "CodeList",
                  "VersionDate": {"DateTime": "2026-08-06T10:27:45.371360683+01:00"},
                  "URN": "urn:ddi:fr.insee:cl-1:1", "Agency": "fr.insee", "ID": "cl-1", "Version": "1",
                  "Label": [{"@language": "fr-FR", "@value": "Liste"}],
                  "Code": [
                    {
                      "$type": "CodeType",
                      "URN": "urn:ddi:fr.insee:code-1:1", "Agency": "fr.insee", "ID": "code-1", "Version": "1",
                      "Value": {"StringValue": "1"},
                      "CategoryReference": {"$type": "Category", "URN": "urn:ddi:fr.insee:cat-1:1", "Agency": "fr.insee", "ID": "cat-1", "Version": "1"}
                    }
                  ]
                }
              ],
              "Category": [
                {
                  "$type": "Category",
                  "VersionDate": {"DateTime": "2026-08-06T10:27:45.371360683+01:00"},
                  "URN": "urn:ddi:fr.insee:cat-1:1", "Agency": "fr.insee", "ID": "cat-1", "Version": "1",
                  "Label": [{"@language": "fr-FR", "@value": "Oui"}]
                }
              ]
            }
            """;

    @BeforeEach
    void setUp() {
        // Le vrai schéma livré, confronté au vrai validateur : c'est tout l'objet de ces tests.
        Ddi4SchemaRepository schemaRepository = new ClasspathDdi4SchemaRepository();
        ddiResources = new DdiResources(ddiService, ddi4toDdi3ConverterService,
                ddi3toDdi4ConverterService, ddiItemConvertService, userProvider, rbacFetcher,
                uriUtils, new DomainDdi4SchemaService(schemaRepository,
                        new NetworkntDdi4SchemaValidator(schemaRepository)));
    }

    @Test
    void shouldRejectTheLegacyColecticaFragmentSet() {
        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(COLECTICA_FRAGMENT_SET);

        assertNotNull(result.getBody());
        assertFalse(result.getBody().valid(),
                "Le format groupé par type ne circule plus : il doit être refusé, pas rattrapé");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void shouldAcceptTheSchemaEnvelope() {
        String envelope =
                """
                {
                  "topLevelReferences": [
                    {"$type": "Category", "URN": "urn:ddi:fr.insee:cat-1:1", "Agency": "fr.insee", "ID": "cat-1", "Version": "1"}
                  ],
                  "items": [
                    {
                      "$type": "Category",
                      "URN": "urn:ddi:fr.insee:cat-1:1", "Agency": "fr.insee", "ID": "cat-1", "Version": "1",
                      "Label": [{"@language": "fr-FR", "@value": "Oui"}]
                    }
                  ]
                }
                """;

        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(envelope);

        assertNotNull(result.getBody());
        assertTrue(result.getBody().valid(),
                "L'enveloppe du schéma doit être acceptée, erreurs : " + result.getBody().errors());
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void shouldReportUnknownTopLevelKey() {
        String withUnknownKey =
                """
                {
                  "topLevelReferences": [],
                  "items": [],
                  "Variabel": []
                }
                """;

        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(withUnknownKey);

        assertNotNull(result.getBody());
        assertFalse(result.getBody().valid());
        assertTrue(result.getBody().errors().stream().anyMatch(e -> e.contains("Variabel")),
                "La clé inconnue doit être signalée, erreurs : " + result.getBody().errors());
    }

    /**
     * Le front renvoie à {@code /validate} ce que le GET lui a servi. Le mapper de l'application
     * n'a pas d'inclusion globale configurée : Jackson sérialise donc les champs optionnels à
     * {@code null}, or le schéma ne déclare aucun type nullable (une propriété absente est valide,
     * une propriété à {@code null} ne l'est pas). Les modèles doivent omettre leurs nuls.
     */
    @Test
    void shouldAcceptGetResponseSerializedByTheApplicationMapper() throws Exception {
        Ddi4Response response = new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(Reference.of("fr.insee", "pi-1", "1", Ddi4PhysicalInstance.TYPE)),
                List.of(new Ddi4PhysicalInstance(
                        Ddi4PhysicalInstance.TYPE,
                        CogsDate.ofDateTime("2026-08-06T13:28:10.283854228+01:00"),
                        "urn:ddi:fr.insee:pi-1:1", "fr.insee", "pi-1", "1",
                        null,
                        new Citation(LangStrings.of("fr-FR", "20260804")),
                        List.of(Reference.of("fr.insee", "dr-1", "1", Ddi4DataRelationship.TYPE)))),
                List.of(new Ddi4DataRelationship(
                        Ddi4DataRelationship.TYPE,
                        CogsDate.ofDateTime("2026-08-06T13:28:10.283854228+01:00"),
                        "urn:ddi:fr.insee:dr-1:1", "fr.insee", "dr-1", "1",
                        null,
                        LangStrings.of("fr-FR", "Structure"),
                        List.of(new LogicalRecord(
                                LogicalRecord.TYPE,
                                "urn:ddi:fr.insee:lr-1:1", "fr.insee", "lr-1", "1",
                                LangStrings.of("fr-FR", "Enregistrement logique"),
                                new VariablesInRecord(List.of(
                                        Reference.of("fr.insee", "var-1", "1", Ddi4Variable.TYPE))))))),
                List.of(new Ddi4Variable(
                        Ddi4Variable.TYPE,
                        CogsDate.ofDateTime("2026-08-06T10:27:45.371360683+01:00"),
                        "urn:ddi:fr.insee:var-1:1", "fr.insee", "var-1", "1",
                        null,
                        LangStrings.of("fr-FR", "copy"),
                        LangStrings.of("fr-FR", "cocpy"),
                        null,
                        new VariableRepresentation(
                                null,
                                new CodeRepresentation(
                                        CodeRepresentation.TYPE,
                                        false,
                                        Reference.of("fr.insee", "cl-1", "1", Ddi4CodeList.TYPE)),
                                null, null, null, null),
                        null)),
                List.of(new Ddi4CodeList(
                        Ddi4CodeList.TYPE,
                        CogsDate.ofDateTime("2026-08-06T10:27:45.371360683+01:00"),
                        "urn:ddi:fr.insee:cl-1:1", "fr.insee", "cl-1", "1",
                        LangStrings.of("fr-FR", "Liste"),
                        null,
                        List.of(new Code(
                                Code.TYPE,
                                "urn:ddi:fr.insee:code-1:1", "fr.insee", "code-1", "1",
                                Reference.of("fr.insee", "cat-1", "1", Ddi4Category.TYPE),
                                ValueType.of("1"),
                                null)))),
                List.of(new Ddi4Category(
                        Ddi4Category.TYPE,
                        CogsDate.ofDateTime("2026-08-06T10:27:45.371360683+01:00"),
                        "urn:ddi:fr.insee:cat-1:1", "fr.insee", "cat-1", "1",
                        LangStrings.of("fr-FR", "Oui"))),
                null);

        // Mapper aux réglages par défaut : celui que Spring Boot utilise pour sérialiser la réponse.
        String asServedByTheGet = new ObjectMapper().writeValueAsString(response);

        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(asServedByTheGet);

        assertNotNull(result.getBody());
        assertTrue(result.getBody().valid(),
                "La réponse du GET doit valider telle quelle, erreurs : " + result.getBody().errors());
    }

    @Test
    void shouldReportInvalidItemInsideTheEnvelope() {
        String withInvalidVariable =
                """
                {
                  "items": [
                    {"$type": "Variable", "URN": "urn:ddi:fr.insee:var-1:1", "Agency": "fr.insee", "ID": "var-1", "Version": "1", "Description": null}
                  ]
                }
                """;

        ResponseEntity<ValidationResponse> result = ddiResources.validateDdi4(withInvalidVariable);

        assertNotNull(result.getBody());
        assertFalse(result.getBody().valid(),
                "Un item non conforme doit être vu : plus aucune enveloppe ne le masque");
    }
}
