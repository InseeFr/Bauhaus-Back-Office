package fr.insee.rmes.bauhaus_services.operations;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.junit.jupiter.api.Assertions;

/** Assertions sur les modèles RDF écrits par les dépôts d'indicateurs et de séries, partagées par leurs tests. */
public final class OperationsRdfModelAssertions {

    private OperationsRdfModelAssertions() {}

    /** Signature de {@code addMulltiLangValues}, commune aux dépôts d'indicateurs et de séries. */
    @FunctionalInterface
    public interface MultiLangValuesWriter {
        void addMulltiLangValues(
                Model model, IRI subject, Resource graph, String valueLg1, String valueLg2, IRI predicate);
    }

    /**
     * Écrit un résumé bilingue « fr » / « en » sous {@code subject} (dont l'IRI doit être
     * http://purl.org/dc/dcmitype/1) et vérifie qu'il ressort en deux littéraux XHTML.
     */
    public static void assertAbstractsWrittenAsPlainMarkdownLiterals(IRI subject, MultiLangValuesWriter writer) {
        Model model = new LinkedHashModel();

        SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();

        writer.addMulltiLangValues(
                model,
                subject,
                simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/"),
                "fr",
                "en",
                DCTERMS.ABSTRACT);

        Assertions.assertEquals(
                model.subjects().toArray()[0], simpleValueFactory.createIRI("http://purl.org/dc/dcmitype/1"));

        Assertions.assertEquals(
                model.predicates().toArray()[0], simpleValueFactory.createIRI(DCTERMS.ABSTRACT.toString()));

        Assertions.assertEquals("\"<p>fr</p>\"@fr", model.objects().toArray()[0].toString());
        Assertions.assertEquals("\"<p>en</p>\"@en", model.objects().toArray()[1].toString());
    }

    /** Le modèle porte, pour ce sujet et ce prédicat, un unique objet : l'IRI attendue. */
    public static void assertSingleIriObject(Model model, IRI subject, IRI predicate, String expectedIri) {
        IRI linkPredicate = SimpleValueFactory.getInstance().createIRI(predicate.toString());
        List<Value> objects = model.filter(subject, linkPredicate, null).stream()
                .map(Statement::getObject)
                .toList();
        assertThat(objects).hasSize(1);
        assertThat(objects.get(0)).isInstanceOf(IRI.class);
        assertThat(objects.get(0).stringValue()).isEqualTo(expectedIri);
    }
}
