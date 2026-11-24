package fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsFetchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsSaveException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesList;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.PersistedCodesList;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.serverside.CodesListsRepository;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DateUtils;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

/**
 * Écriture RDF d'une liste de codes complète.
 * <p>
 * Les IRI sont construites exactement comme le faisait {@code CodeListServiceImpl} — la liste par
 * {@code lastListUriSegment}, sa classe OWL par {@code concept/lastClassUriSegment} — pour que les
 * listes créées avant et après la migration soient indiscernables en base.
 */
@ServerSideAdaptor
@Repository
public class GraphDBCodesListsRepository implements CodesListsRepository {
    private final BauhausIriFactory iriFactory;

    /** Segment d'URI de la classe OWL associée à une liste complète. */
    private static final String CONCEPT = "concept/";

    private final RepositoryGestion repositoryGestion;
    private final CodeListsQueries codeListsQueries;
    private final BauhausLanguagesProperties languages;

    public GraphDBCodesListsRepository(
            RepositoryGestion repositoryGestion,
            CodeListsQueries codeListsQueries,
            BauhausLanguagesProperties languages,
            BauhausIriFactory iriFactory) {
        this.repositoryGestion = repositoryGestion;
        this.codeListsQueries = codeListsQueries;
        this.languages = languages;
        this.iriFactory = iriFactory;
    }

    @Override
    public boolean isIdentityAlreadyTaken(CodesList codesList) throws CodesListsFetchException {
        try {
            String owlClassIri = owlClassIri(codesList).toString();
            return repositoryGestion.getResponseAsBoolean(codeListsQueries.checkCodeListUnicity(
                    codesList.id().value(), iriOf(codesList).toString(), owlClassIri, CodeListKind.FULL));
        } catch (RmesException e) {
            throw new CodesListsFetchException(e);
        }
    }

    @Override
    public Optional<PersistedCodesList> findByUriSegment(String lastListUriSegment) throws CodesListsFetchException {
        try {
            IRI iri = iriFactory.codeList(lastListUriSegment);
            JSONObject persisted =
                    repositoryGestion.getResponseAsObject(codeListsQueries.getCodesListByIri(iri.toString()));
            if (persisted.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new PersistedCodesList(
                    persisted.optString("created", null),
                    ValidationStatus.fromValue(persisted.optString("validationState", null))));
        } catch (RmesException e) {
            throw new CodesListsFetchException(e);
        }
    }

    @Override
    public void save(CodesList codesList) throws CodesListsSaveException {
        IRI codesListIri = iriOf(codesList);
        IRI owlClassIri = owlClassIri(codesList);
        Resource graph = iriFactory.codesListGraph();
        Model model = new LinkedHashModel();

        // La date de création n'appartient pas au client : à la mise à jour, le domaine a rendu
        // celle qui est en base ; à la création, il n'y en a pas encore.
        RdfTriples.addDateTime(
                codesListIri,
                DCTERMS.CREATED,
                codesList.created() == null ? DateUtils.getCurrentDate() : codesList.created(),
                model,
                graph);
        RdfTriples.addDateTime(codesListIri, DCTERMS.MODIFIED, DateUtils.getCurrentDate(), model, graph);

        model.add(codesListIri, INSEE.VALIDATION_STATE, RdfTriples.string(codesList.validationState()), graph);
        RdfTriples.addUri(codesListIri, RDF.TYPE, SKOS.CONCEPT_SCHEME, model, graph);
        model.add(codesListIri, SKOS.NOTATION, RdfTriples.string(codesList.id().value()), graph);
        RdfTriples.addUri(codesListIri, INSEE.DISSEMINATIONSTATUS, codesList.disseminationStatus(), model, graph);

        model.add(codesListIri, SKOS.PREF_LABEL, RdfTriples.string(codesList.labelLg1(), languages.lg1()), graph);
        model.add(codesListIri, SKOS.PREF_LABEL, RdfTriples.string(codesList.labelLg2(), languages.lg2()), graph);

        if (codesList.descriptionLg1() != null) {
            model.add(
                    codesListIri,
                    SKOS.DEFINITION,
                    RdfTriples.string(codesList.descriptionLg1(), languages.lg1()),
                    graph);
        }
        if (codesList.descriptionLg2() != null) {
            model.add(
                    codesListIri,
                    SKOS.DEFINITION,
                    RdfTriples.string(codesList.descriptionLg2(), languages.lg2()),
                    graph);
        }

        RdfTriples.addUri(codesListIri, DC.CREATOR, codesList.creator(), model, graph);
        codesList
                .contributors()
                .forEach(contributor -> RdfTriples.addUri(codesListIri, DC.CONTRIBUTOR, contributor, model, graph));

        RdfTriples.addString(codesListIri, INSEE.LAST_CODE_URI_SEGMENT, codesList.lastCodeUriSegment(), model, graph);
        RdfTriples.addUri(codesListIri, RDFS.SEEALSO, owlClassIri, model, graph);
        RdfTriples.addUri(owlClassIri, RDF.TYPE, OWL.CLASS, model, graph);
        RdfTriples.addUri(owlClassIri, RDFS.SEEALSO, codesListIri, model, graph);

        try {
            repositoryGestion.clearStructureNodeAndComponents(codesListIri, graph);
            repositoryGestion.loadSimpleObject(codesListIri, model, null);
        } catch (RmesException e) {
            throw new CodesListsSaveException(e);
        }
    }

    private IRI iriOf(CodesList codesList) {
        return iriFactory.codeList(codesList.lastListUriSegment());
    }

    private IRI owlClassIri(CodesList codesList) {
        return iriFactory.codeList(CONCEPT + codesList.lastClassUriSegment());
    }
}
