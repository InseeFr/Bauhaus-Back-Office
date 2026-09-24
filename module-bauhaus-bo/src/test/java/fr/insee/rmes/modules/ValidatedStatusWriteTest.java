package fr.insee.rmes.modules;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Seul {@code ValidationStatusListener} écrit le statut « Validated » : les services publient
 * l'événement {@code ObjectPublished} au lieu d'écrire le statut eux-mêmes.
 * <p>
 * Le contrôle lit les sources, pas le bytecode : il doit distinguer l'écriture de la lecture, et la
 * moitié des usages de {@code ValidationStatus.VALIDATED} sont des lectures (comparaisons d'état), qui
 * restent permises. Une écriture prend l'une de ces deux formes :
 * <ul>
 *     <li>le littéral du statut, {@code setLiteralString(ValidationStatus.VALIDATED)} ou
 *     {@code RdfTriples.string(ValidationStatus.VALIDATED)}, ajouté au modèle ;</li>
 *     <li>la régénération d'un objet avec le statut en paramètre,
 *     {@code createRdf…(…, ValidationStatus.VALIDATED)}.</li>
 * </ul>
 * La règle est gelée : {@link #FROZEN} liste les écritures pas encore migrées vers l'événement. Il ne
 * doit que décroître, objet après objet ; une fois vide, il disparaît avec lui.
 */
class ValidatedStatusWriteTest {

    private static final Path MODULES = Path.of("..");

    private static final String LISTENER =
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/modules/shared_kernel/infrastructure/publication/ValidationStatusListener.java";

    private static final Set<String> FROZEN = Set.of(
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/classifications/ClassificationsServiceImpl.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/code_list/CodeListServiceImpl.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/concepts/collections/LegacyCollectionsRepository.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/concepts/concepts/LegacyConceptsRepository.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/datasets/DatasetServiceImpl.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/distribution/DistributionServiceImpl.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/operations/documentations/DocumentationsUtils.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/operations/indicators/IndicatorsRepository.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/operations/operations/OperationsRepository.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/operations/series/SeriesRepository.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/structures/persistence/StructureComponentRepository.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/bauhaus_services/structures/persistence/StructureRepository.java",
            "module-bauhaus-bo/src/main/java/fr/insee/rmes/modules/operations/families/infrastructure/graphdb/GraphDBOperationFamilyRepository.java");

    private static final Pattern VALIDATED_WRITE =
            Pattern.compile("(?:setLiteralString|string)\\(\\s*ValidationStatus\\.VALIDATED\\s*\\)"
                    + "|create(?:Rdf|RDF)\\w*\\([^;]*?ValidationStatus\\.VALIDATED");

    @Test
    void only_the_listener_should_write_the_validated_status() {
        Set<String> newWriters = new TreeSet<>(writers());
        newWriters.removeAll(FROZEN);
        newWriters.remove(LISTENER);

        assertThat(newWriters).describedAs("""
                        Ces classes écrivent le statut Validated : publier ObjectPublished à la place, \
                        ValidationStatusListener se charge du statut.""").isEmpty();
    }

    @Test
    void frozen_writers_should_still_write_the_validated_status() {
        Set<String> migrated = new TreeSet<>(FROZEN);
        migrated.removeAll(writers());

        assertThat(migrated)
                .describedAs("Ces classes n'écrivent plus le statut Validated : les retirer de FROZEN.")
                .isEmpty();
    }

    @Test
    void the_listener_should_be_found_as_a_writer() {
        assertThat(writers())
                .describedAs("Sans quoi le motif ne reconnaît plus aucune écriture et la règle ne protège plus rien.")
                .contains(LISTENER);
    }

    private static Set<String> writers() {
        try (Stream<Path> files = Files.walk(MODULES)) {
            return files.filter(file -> file.toString().endsWith(".java"))
                    .map(MODULES::relativize)
                    .filter(file -> file.toString().replace('\\', '/').contains("/src/main/java/"))
                    .filter(file -> VALIDATED_WRITE.matcher(sourceOf(file)).find())
                    .map(file -> file.toString().replace('\\', '/'))
                    .collect(Collectors.toCollection(TreeSet::new));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String sourceOf(Path file) {
        try {
            return Files.readString(MODULES.resolve(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
