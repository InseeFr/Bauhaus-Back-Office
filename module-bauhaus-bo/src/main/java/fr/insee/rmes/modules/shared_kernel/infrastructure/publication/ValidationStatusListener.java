package fr.insee.rmes.modules.shared_kernel.infrastructure.publication;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Passe un objet publié à l'état « Validated » : seul endroit du code qui écrit ce statut.
 * <p>
 * L'écoute est synchrone et toujours active, sans condition de module ni de propriété : si le statut
 * ne peut être écrit, la requête de publication échoue.
 * <p>
 * Le statut est remplacé dans le seul graphe de gestion reçu, contrairement à
 * {@code RepositoryGestion.objectValidation}, qui l'efface dans tous les graphes.
 */
@Component
public class ValidationStatusListener {

    private final RepositoryGestion repoGestion;

    public ValidationStatusListener(RepositoryGestion repoGestion) {
        this.repoGestion = repoGestion;
    }

    @EventListener
    public void on(ObjectPublished event) throws RmesException {
        repoGestion.deleteTripletByPredicate(event.subject(), INSEE.VALIDATION_STATE, event.managementGraph());

        Model model = new LinkedHashModel();
        model.add(
                event.subject(),
                INSEE.VALIDATION_STATE,
                RdfTriples.string(ValidationStatus.VALIDATED),
                event.managementGraph());
        repoGestion.loadSimpleObjectWithoutDeletion(event.subject(), model, null);
    }
}
