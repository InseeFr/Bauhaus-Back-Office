package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.DocumentationsProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.DatableNote;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.VersionableNote;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.stereotype.Component;

/**
 * IRI et graphes de Bauhaus qui se déduisent de la configuration.
 *
 * <p>Les propriétés arrivent par le constructeur : le conteneur Spring garantit que la fabrique
 * est complète avant d'être utilisée, et un test se contente de lui passer les propriétés qu'il
 * veut.
 */
@Component
public class BauhausIriFactory {

    private final GraphsProperties graphs;
    private final BauhausUriBuilder uriBuilder;
    private final DocumentationsProperties documentations;

    public BauhausIriFactory(
            GraphsProperties graphs, BauhausUriBuilder uriBuilder, DocumentationsProperties documentations) {
        this.graphs = graphs;
        this.uriBuilder = uriBuilder;
        this.documentations = documentations;
    }

    public Resource structureComponentGraph() {
        return RdfTriples.iri(graphs.structuresComponentsGraph());
    }

    public IRI structureComponentAttribute(String id) {
        return objectIri(ObjectType.ATTRIBUTE_PROPERTY, id);
    }

    public IRI structureComponentDimension(String id) {
        return objectIri(ObjectType.DIMENSION_PROPERTY, id);
    }

    public IRI structureComponentMeasure(String id) {
        return objectIri(ObjectType.MEASURE_PROPERTY, id);
    }

    /**
     * URI de base des concepts, à laquelle un identifiant de concept est concaténé. Renvoyée sous
     * forme de chaîne, et non d'IRI, parce que ce préfixe n'est pas lui-même une ressource.
     */
    public String conceptBaseUri() {
        return uriBuilder.getBaseUriGestion(ObjectType.CONCEPT);
    }

    /**
     * IRI de gestion de l'objet d'identifiant donné. Un identifiant déjà absolu est repris tel
     * quel.
     */
    public IRI objectIri(ObjectType objectType, String id) {
        if (isAbsoluteUri(id)) {
            return RdfTriples.iri(id);
        }
        return RdfTriples.iri(uriBuilder.getBaseUriGestion(objectType) + "/" + id);
    }

    public Resource conceptGraph() {
        return RdfTriples.iri(graphs.conceptsGraph());
    }

    public IRI concept(String id) {
        return objectIri(ObjectType.CONCEPT, id);
    }

    public IRI collection(String id) {
        return objectIri(ObjectType.COLLECTION, id);
    }

    public IRI conceptScheme() {
        return RdfTriples.iri(uriBuilder.baseUriGestion() + documentations.conceptsScheme());
    }

    /**
     * IRI de publication de l'objet d'identifiant donné. Un identifiant déjà absolu est repris tel
     * quel.
     */
    public IRI objectIriPublication(ObjectType objectType, String id) {
        if (isAbsoluteUri(id)) {
            return RdfTriples.iri(id);
        }
        return RdfTriples.iri(uriBuilder.getBaseUriPublication(objectType) + "/" + id);
    }

    public IRI versionableNote(String conceptId, VersionableNote note) {
        return versionableNote(conceptId, note, note.getVersion());
    }

    public IRI previousVersionableNote(String conceptId, VersionableNote note) {
        return versionableNote(conceptId, note, String.valueOf(Integer.parseInt(note.getVersion()) - 1));
    }

    private IRI versionableNote(String conceptId, VersionableNote note, String version) {
        return RdfTriples.iri(
                conceptBaseUri() + "/" + conceptId + "/" + note.getPath() + "/v" + version + "/" + note.getLang());
    }

    /** IRI d'une note datée : la date qu'elle porte est celle du jour de l'écriture. */
    public IRI datableNote(String conceptId, DatableNote note) {
        String today = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
        return RdfTriples.iri(
                conceptBaseUri() + "/" + conceptId + "/" + note.getPath() + "/" + today + "/" + note.getLang());
    }

    public Resource operationsGraph() {
        return RdfTriples.iriAsIs(graphs.operationsGraph());
    }

    public Resource productsGraph() {
        return RdfTriples.iriAsIs(graphs.productsGraph());
    }

    public Resource documentsGraph() {
        return RdfTriples.iriAsIs(graphs.documentsGraph());
    }

    public Resource simsGraph(String id) {
        return RdfTriples.iriAsIs(graphs.documentationsGraph() + "/" + id);
    }

    public Resource simsGeographyGraph() {
        return RdfTriples.iriAsIs(graphs.documentationsGeoGraph());
    }

    public Resource organizationsGraph() {
        return RdfTriples.iriAsIs(graphs.organizationsGraph());
    }

    public Resource orgInseeGraph() {
        return RdfTriples.iriAsIs(graphs.orgInseeGraph());
    }

    public IRI family(String id) {
        return objectIri(ObjectType.FAMILY, id);
    }

    public IRI series(String id) {
        return objectIri(ObjectType.SERIES, id);
    }

    public IRI operation(String id) {
        return objectIri(ObjectType.OPERATION, id);
    }

    public IRI document(String id) {
        return objectIri(ObjectType.DOCUMENT, id);
    }

    public IRI link(String id) {
        return objectIri(ObjectType.LINK, id);
    }

    public Resource structureGraph() {
        return RdfTriples.iriAsIs(graphs.structuresGraph());
    }

    public Resource codesListGraph() {
        return RdfTriples.iriAsIs(graphs.codeListGraph());
    }

    public Resource codesListGraph(String id) {
        return RdfTriples.iriAsIs(graphs.codeListGraph() + "/" + id);
    }

    public IRI structure(String id) {
        return objectIri(ObjectType.STRUCTURE, id);
    }

    public IRI codeList(String id) {
        return objectIri(ObjectType.CODE_LIST, id);
    }

    public IRI classificationSerie(String id) {
        return RdfTriples.iriAsIs(uriBuilder.baseUriGestion() + "codes/serieDeNomenclatures/" + id);
    }

    /** IRI de la définition de composant d'identifiant donné, rangée sous l'IRI de sa structure. */
    public IRI structureComponentDefinition(String structureIri, String componentDefinitionId) {
        return RdfTriples.iriAsIs(structureIri + "/" + componentDefinitionId);
    }

    private static boolean isAbsoluteUri(String value) {
        return value != null && (value.startsWith("http://") || value.startsWith("https://"));
    }
}
