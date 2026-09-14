package fr.insee.rmes.modules.ddi.operations_mirror;

import fr.insee.rmes.modules.ddi.operations_mirror.domain.port.clientside.OperationsMirrorService;
import fr.insee.rmes.modules.operation.domain.event.OperationSaved;
import fr.insee.rmes.modules.operation.domain.event.SeriesSaved;
import org.springframework.context.event.EventListener;

/**
 * Relaie au miroir DDI les écritures annoncées par le domaine « opérations ».
 * <p>
 * L'écoute est synchrone et ne rattrape rien : une défaillance du dépôt DDI fait échouer la requête
 * qui a déclenché l'écriture, plutôt que de laisser les deux dépôts diverger en silence.
 */
public class OperationsMirrorEventListener {

    private final OperationsMirrorService mirror;

    public OperationsMirrorEventListener(OperationsMirrorService mirror) {
        this.mirror = mirror;
    }

    @EventListener
    public void on(SeriesSaved event) {
        mirror.mirror(event);
    }

    @EventListener
    public void on(OperationSaved event) {
        mirror.mirror(event);
    }
}
