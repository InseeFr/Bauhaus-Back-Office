package fr.insee.rmes.modules.codeslists.codeslists.domain;

import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListAlreadyExistsException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListIdMismatchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListNotFoundException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsFetchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsSaveException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesList;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesListId;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.PersistedCodesList;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.CreateCodesListCommand;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.UpdateCodesListCommand;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.serverside.CodesListsRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Règles d'écriture d'une liste de codes complète, vues du domaine : ce que le client ne décide
 * pas (date de création, état de publication) et ce qu'il ne peut pas contourner (unicité,
 * cohérence de l'url, existence de la liste mise à jour).
 */
class DomainCodesListsServiceTest {

    /** Dépôt en mémoire : le domaine se teste sans triplestore ni mock à réglages. */
    private static class InMemoryCodesListsRepository implements CodesListsRepository {
        private final List<CodesList> saved = new ArrayList<>();
        private boolean identityTaken = false;
        private Optional<PersistedCodesList> persisted = Optional.empty();

        @Override
        public boolean isIdentityAlreadyTaken(CodesList codesList) {
            return identityTaken;
        }

        @Override
        public Optional<PersistedCodesList> findByUriSegment(String lastListUriSegment) {
            return persisted;
        }

        @Override
        public void save(CodesList codesList) {
            saved.add(codesList);
        }

        CodesList onlySaved() {
            assertThat(saved).hasSize(1);
            return saved.getFirst();
        }
    }

    private final InMemoryCodesListsRepository repository = new InMemoryCodesListsRepository();
    private final DomainCodesListsService service = new DomainCodesListsService(repository);

    private static CreateCodesListCommand command(String id) {
        return new CreateCodesListCommand(id, "libellé", "label", null, null,
                "http://bauhaus/HIE000000", List.of("http://bauhaus/HIE000001"),
                "http://disseminationStatus", "cl-test", "ClTest", "cl-test-code");
    }

    @Test
    void a_created_codes_list_is_unpublished_and_has_no_creation_date_of_its_own() throws Exception {
        CodesListId id = service.create(command("CL_TEST"));

        assertThat(id).isEqualTo(new CodesListId("CL_TEST"));
        CodesList saved = repository.onlySaved();
        assertThat(saved.validationState()).isEqualTo(ValidationStatus.UNPUBLISHED);
        // L'horodatage revient à l'infrastructure : le domaine ne décide pas de « maintenant ».
        assertThat(saved.created()).isNull();
        assertThat(saved.contributors()).containsExactly("http://bauhaus/HIE000001");
    }

    @Test
    void creating_a_codes_list_whose_identity_is_taken_is_rejected_before_any_write() {
        repository.identityTaken = true;

        assertThatThrownBy(() -> service.create(command("CL_TEST")))
                .isInstanceOf(CodesListAlreadyExistsException.class);

        assertThat(repository.saved).isEmpty();
    }

    @Test
    void updating_a_codes_list_that_does_not_exist_is_a_not_found_and_not_a_silent_upsert() {
        repository.persisted = Optional.empty();

        assertThatThrownBy(() -> service.update(new CodesListId("CL_TEST"), new UpdateCodesListCommand(command("CL_TEST"))))
                .isInstanceOf(CodesListNotFoundException.class);

        assertThat(repository.saved).isEmpty();
    }

    @Test
    void updating_through_an_url_that_names_another_codes_list_is_rejected() {
        repository.persisted = Optional.of(new PersistedCodesList("2020-01-01T00:00:00", ValidationStatus.UNPUBLISHED));

        assertThatThrownBy(() -> service.update(new CodesListId("CL_TEST"), new UpdateCodesListCommand(command("CL_OTHER"))))
                .isInstanceOf(CodesListIdMismatchException.class);

        assertThat(repository.saved).isEmpty();
    }

    @Test
    void updating_keeps_the_persisted_creation_date() throws Exception {
        repository.persisted = Optional.of(new PersistedCodesList("2020-01-01T00:00:00", ValidationStatus.UNPUBLISHED));

        service.update(new CodesListId("CL_TEST"), new UpdateCodesListCommand(command("CL_TEST")));

        assertThat(repository.onlySaved().created()).isEqualTo("2020-01-01T00:00:00");
    }

    /**
     * L'état de publication se lit en base, pas dans le corps de la requête : une liste déjà
     * publiée puis modifiée reste « Modified », elle ne retombe pas à « Unpublished ».
     */
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "UNPUBLISHED, UNPUBLISHED",
            "VALIDATED, MODIFIED",
            "MODIFIED, MODIFIED"
    })
    void updating_moves_the_validation_state(ValidationStatus persistedState, ValidationStatus expected)
            throws CodesListIdMismatchException, CodesListNotFoundException, CodesListsFetchException, CodesListsSaveException {
        repository.persisted = Optional.of(new PersistedCodesList("2020-01-01T00:00:00", persistedState));

        service.update(new CodesListId("CL_TEST"), new UpdateCodesListCommand(command("CL_TEST")));

        assertThat(repository.onlySaved().validationState()).isEqualTo(expected);
    }
}
