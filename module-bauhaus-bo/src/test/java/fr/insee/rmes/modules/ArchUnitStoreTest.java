package fr.insee.rmes.modules;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Garde-fou sur le contenu de {@code archunit_store}.
 * <p>
 * Quand la description d'une règle gelée change (ajout d'un package autorisé, reformulation du
 * {@code because}...), ArchUnit crée une nouvelle entrée dans le store et laisse l'ancienne en
 * place : le store grossit et conserve des baselines de violations qui ne protègent plus rien.
 * Ces tests échouent dès qu'une entrée du store ne correspond plus à une règle gelée déclarée,
 * ou qu'un fichier de violations n'est plus référencé.
 */
class ArchUnitStoreTest {

    private static final Path STORE = Path.of("archunit_store");
    private static final String STORED_RULES_FILE = "stored.rules";

    @Test
    void store_should_not_contain_obsolete_rules() {
        Set<String> obsolete = new TreeSet<>(storedRules().stringPropertyNames());
        obsolete.removeAll(declaredFrozenRuleDescriptions());

        assertThat(obsolete).describedAs("""
                        Ces règles sont encore dans %s/%s alors qu'aucune règle gelée du code ne porte \
                        cette description (règle renommée, reformulée ou supprimée). Supprimer la ligne \
                        correspondante de stored.rules ainsi que le fichier de violations associé.""", STORE, STORED_RULES_FILE).isEmpty();
    }

    @Test
    void store_should_not_contain_orphan_violation_files() throws IOException {
        Properties storedRules = storedRules();
        Set<String> referencedFiles = storedRules.stringPropertyNames().stream()
                .map(storedRules::getProperty)
                .collect(Collectors.toSet());

        Set<String> orphans;
        try (Stream<Path> files = Files.list(STORE)) {
            orphans = files.map(file -> file.getFileName().toString())
                    .filter(name -> !STORED_RULES_FILE.equals(name))
                    .filter(name -> !referencedFiles.contains(name))
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        assertThat(orphans)
                .describedAs(
                        "Fichiers de violations orphelins dans %s : plus référencés par %s, à supprimer",
                        STORE, STORED_RULES_FILE)
                .isEmpty();
    }

    private static Properties storedRules() {
        Path storedRulesPath = STORE.resolve(STORED_RULES_FILE);
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(storedRulesPath)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire " + storedRulesPath, e);
        }
        return properties;
    }

    /**
     * Descriptions des règles gelées effectivement déclarées dans les tests du module : ce sont
     * elles qui servent de clé dans {@code stored.rules}.
     */
    private static Set<String> declaredFrozenRuleDescriptions() {
        JavaClasses testClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.ONLY_INCLUDE_TESTS)
                .importPackages("fr.insee.rmes");

        return testClasses.stream()
                .flatMap(testClass -> testClass.getFields().stream()
                        .filter(ArchUnitStoreTest::isStaticArchTestField)
                        .map(field -> readStaticValue(testClass, field)))
                .filter(FreezingArchRule.class::isInstance)
                .map(rule -> ((FreezingArchRule) rule).getDescription())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static boolean isStaticArchTestField(JavaField field) {
        return field.isAnnotatedWith(ArchTest.class) && field.getModifiers().contains(JavaModifier.STATIC);
    }

    private static Object readStaticValue(JavaClass testClass, JavaField field) {
        try {
            Field reflectedField = Class.forName(testClass.getName()).getDeclaredField(field.getName());
            reflectedField.setAccessible(true);
            return reflectedField.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Impossible de lire la règle " + testClass.getName() + "." + field.getName(), e);
        }
    }
}
