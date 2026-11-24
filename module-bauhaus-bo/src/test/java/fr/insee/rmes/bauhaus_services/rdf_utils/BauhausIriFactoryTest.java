package fr.insee.rmes.bauhaus_services.rdf_utils;

import static fr.insee.rmes.PropertiesKeys.CODE_LIST_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.COLLECTIONS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.CONCEPTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.DOCUMENTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.LINKS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.OPERATIONS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.OP_FAMILIES_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.OP_SERIES_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.STRUCTURES_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.STRUCTURES_COMPONENTS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.DocumentationsProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.DatableNote;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.VersionableNote;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

/**
 * Contrat de {@link BauhausIriFactory} : les IRI et les graphes qui dépendent de la
 * configuration.
 */
class BauhausIriFactoryTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

    private final BauhausIriFactory iriFactory = new BauhausIriFactory(
            GraphsPropertiesStub.stub(),
            new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> switch (name) {
                case STRUCTURES_COMPONENTS_BASE_URI -> Optional.of("composants/");
                case CONCEPTS_BASE_URI -> Optional.of("concepts/definition");
                case COLLECTIONS_BASE_URI -> Optional.of("concepts/collection");
                case OP_FAMILIES_BASE_URI -> Optional.of("operations/famille");
                case OP_SERIES_BASE_URI -> Optional.of("operations/serie");
                case OPERATIONS_BASE_URI -> Optional.of("operations/operation");
                case DOCUMENTS_BASE_URI -> Optional.of("documents/document");
                case LINKS_BASE_URI -> Optional.of("documents/lien");
                case STRUCTURES_BASE_URI -> Optional.of("structuresDeDonnees/structure");
                case CODE_LIST_BASE_URI -> Optional.of("codes");
                default -> Optional.empty();
            }),
            new DocumentationsProperties("", "", "concepts/definitions/scheme"));

    @Test
    void structureComponentGraph_is_the_configured_components_graph() {
        assertThat(iriFactory.structureComponentGraph())
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/composants"));
    }

    @Test
    void attribute_iri_is_built_from_the_components_base_uri() {
        assertThat(iriFactory.structureComponentAttribute("a1000"))
                .isEqualTo(VF.createIRI("http://bauhaus/composants/attribut/a1000"));
    }

    @Test
    void dimension_iri_is_built_from_the_components_base_uri() {
        assertThat(iriFactory.structureComponentDimension("d1000"))
                .isEqualTo(VF.createIRI("http://bauhaus/composants/dimension/d1000"));
    }

    @Test
    void measure_iri_is_built_from_the_components_base_uri() {
        assertThat(iriFactory.structureComponentMeasure("m1000"))
                .isEqualTo(VF.createIRI("http://bauhaus/composants/mesure/m1000"));
    }

    /**
     * Un identifiant déjà absolu est une IRI complète : elle est reprise telle quelle, sans être
     * préfixée une seconde fois par l'URI de base.
     */
    @Test
    void an_absolute_identifier_is_kept_as_is() {
        assertThat(iriFactory.structureComponentDimension("http://autre/composant/d1000"))
                .isEqualTo(VF.createIRI("http://autre/composant/d1000"));
    }

    @Test
    void conceptBaseUri_is_the_gestion_base_uri_of_concepts() {
        assertThat(iriFactory.conceptBaseUri()).isEqualTo("http://bauhaus/concepts/definition");
    }

    @Test
    void concept_graph_is_the_configured_concepts_graph() {
        assertThat(iriFactory.conceptGraph()).isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/concepts/"));
    }

    @Test
    void concept_iri_is_built_from_the_concepts_base_uri() {
        assertThat(iriFactory.concept("c1000")).isEqualTo(VF.createIRI("http://bauhaus/concepts/definition/c1000"));
    }

    @Test
    void collection_iri_is_built_from_the_collections_base_uri() {
        assertThat(iriFactory.collection("col1")).isEqualTo(VF.createIRI("http://bauhaus/concepts/collection/col1"));
    }

    @Test
    void concept_scheme_is_the_configured_scheme_under_the_gestion_base_uri() {
        assertThat(iriFactory.conceptScheme()).isEqualTo(VF.createIRI("http://bauhaus/concepts/definitions/scheme"));
    }

    @Test
    void object_iri_publication_is_built_from_the_publication_base_uri() {
        assertThat(iriFactory.objectIriPublication(ObjectType.CONCEPT, "c1000"))
                .isEqualTo(VF.createIRI("http://publication/concepts/definition/c1000"));
    }

    @Test
    void object_iri_publication_keeps_an_absolute_identifier_as_is() {
        assertThat(iriFactory.objectIriPublication(ObjectType.CONCEPT, "https://autre/c1000"))
                .isEqualTo(VF.createIRI("https://autre/c1000"));
    }

    @Test
    void versionable_note_iri_carries_the_note_path_version_and_language() {
        assertThat(iriFactory.versionableNote("c1000", versionableNote("3")))
                .isEqualTo(VF.createIRI("http://bauhaus/concepts/definition/c1000/definition/v3/fr"));
    }

    @Test
    void previous_versionable_note_iri_points_to_the_preceding_version() {
        assertThat(iriFactory.previousVersionableNote("c1000", versionableNote("3")))
                .isEqualTo(VF.createIRI("http://bauhaus/concepts/definition/c1000/definition/v2/fr"));
    }

    @Test
    void datable_note_iri_is_dated_of_today() {
        DatableNote note = new DatableNote();
        note.setPath("noteChangement");
        note.setLang("en");
        String today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());

        assertThat(iriFactory.datableNote("c1000", note))
                .isEqualTo(VF.createIRI("http://bauhaus/concepts/definition/c1000/noteChangement/" + today + "/en"));
    }

    @Test
    void operations_graph_is_the_configured_operations_graph() {
        assertThat(iriFactory.operationsGraph()).isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/operations"));
    }

    @Test
    void products_graph_is_the_configured_products_graph() {
        assertThat(iriFactory.productsGraph()).isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/produits"));
    }

    @Test
    void documents_graph_is_the_configured_documents_graph() {
        assertThat(iriFactory.documentsGraph())
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/qualite/documents"));
    }

    /** Chaque rapport SIMS vit dans son propre graphe, suffixé par son identifiant. */
    @Test
    void sims_graph_is_the_documentations_graph_suffixed_by_the_sims_id() {
        assertThat(iriFactory.simsGraph("1500"))
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/qualite/rapport/1500"));
    }

    @Test
    void sims_geography_graph_is_the_configured_territories_graph() {
        assertThat(iriFactory.simsGeographyGraph())
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/qualite/territoires"));
    }

    @Test
    void organizations_graph_is_the_configured_organizations_graph() {
        assertThat(iriFactory.organizationsGraph())
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/organisations"));
    }

    @Test
    void org_insee_graph_is_the_configured_insee_organizations_graph() {
        assertThat(iriFactory.orgInseeGraph())
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/organisations/insee"));
    }

    @Test
    void family_iri_is_built_from_the_families_base_uri() {
        assertThat(iriFactory.family("s1")).isEqualTo(VF.createIRI("http://bauhaus/operations/famille/s1"));
    }

    @Test
    void series_iri_is_built_from_the_series_base_uri() {
        assertThat(iriFactory.series("s1000")).isEqualTo(VF.createIRI("http://bauhaus/operations/serie/s1000"));
    }

    @Test
    void operation_iri_is_built_from_the_operations_base_uri() {
        assertThat(iriFactory.operation("s1500")).isEqualTo(VF.createIRI("http://bauhaus/operations/operation/s1500"));
    }

    @Test
    void document_iri_is_built_from_the_documents_base_uri() {
        assertThat(iriFactory.document("1000")).isEqualTo(VF.createIRI("http://bauhaus/documents/document/1000"));
    }

    @Test
    void link_iri_is_built_from_the_links_base_uri() {
        assertThat(iriFactory.link("1000")).isEqualTo(VF.createIRI("http://bauhaus/documents/lien/1000"));
    }

    @Test
    void structure_graph_is_the_configured_structures_graph() {
        assertThat(iriFactory.structureGraph()).isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/structures"));
    }

    @Test
    void codes_list_graph_is_the_configured_codes_graph() {
        assertThat(iriFactory.codesListGraph()).isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/codes"));
    }

    /** Chaque nomenclature vit dans son propre graphe, suffixé par son identifiant. */
    @Test
    void codes_list_graph_of_an_id_is_the_codes_graph_suffixed_by_the_id() {
        assertThat(iriFactory.codesListGraph("naf2008"))
                .isEqualTo(VF.createIRI("http://rdf.insee.fr/graphes/codes/naf2008"));
    }

    @Test
    void structure_iri_is_built_from_the_structures_base_uri() {
        assertThat(iriFactory.structure("dsd1000"))
                .isEqualTo(VF.createIRI("http://bauhaus/structuresDeDonnees/structure/dsd1000"));
    }

    @Test
    void code_list_iri_is_built_from_the_code_lists_base_uri() {
        assertThat(iriFactory.codeList("CL_SEXE")).isEqualTo(VF.createIRI("http://bauhaus/codes/CL_SEXE"));
    }

    @Test
    void classification_serie_iri_is_built_under_the_gestion_base_uri() {
        assertThat(iriFactory.classificationSerie("nafr2"))
                .isEqualTo(VF.createIRI("http://bauhaus/codes/serieDeNomenclatures/nafr2"));
    }

    @Test
    void structure_component_definition_iri_is_the_component_id_under_the_structure_iri() {
        assertThat(iriFactory.structureComponentDefinition("http://bauhaus/structure/dsd1", "cs1"))
                .isEqualTo(VF.createIRI("http://bauhaus/structure/dsd1/cs1"));
    }

    private static VersionableNote versionableNote(String version) {
        VersionableNote note = new VersionableNote();
        note.setPath("definition");
        note.setVersion(version);
        note.setLang("fr");
        return note;
    }
}
