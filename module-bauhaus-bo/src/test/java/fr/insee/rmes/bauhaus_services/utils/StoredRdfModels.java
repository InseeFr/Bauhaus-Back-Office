package fr.insee.rmes.bauhaus_services.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.mockito.ArgumentCaptor;

/**
 * Lecture du modèle RDF qu'un service a confié à {@link RepositoryGestion}, partagée par les tests
 * des services qui écrivent listes de codes, territoires et structures.
 */
public final class StoredRdfModels {

    private StoredRdfModels() {}

    /** Le modèle chargé par l'unique appel à {@code loadSimpleObject(iri, model, null)}. */
    public static Model storedModel(RepositoryGestion repoGestion) throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadSimpleObject(any(IRI.class), modelCaptor.capture(), isNull());
        return modelCaptor.getValue();
    }

    /** Les valeurs de tous les objets portés par {@code predicate}. */
    public static List<String> objectsOf(Model model, IRI predicate) {
        return model.stream()
                .filter(statement -> statement.getPredicate().equals(predicate))
                .map(statement -> statement.getObject().stringValue())
                .toList();
    }

    /** La valeur du premier objet porté par {@code predicate} ; échoue s'il n'y en a aucun. */
    public static String objectOf(Model model, IRI predicate) {
        return model.stream()
                .filter(statement -> statement.getPredicate().equals(predicate))
                .map(statement -> statement.getObject().stringValue())
                .findFirst()
                .orElseThrow(() -> new AssertionError("no statement for " + predicate));
    }
}
