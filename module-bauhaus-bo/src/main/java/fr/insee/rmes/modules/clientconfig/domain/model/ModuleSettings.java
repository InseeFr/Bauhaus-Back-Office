package fr.insee.rmes.modules.clientconfig.domain.model;

/**
 * Les réglages d'un module, déclarés sous son identifiant dans
 * {@code fr.insee.rmes.bauhaus.modules}.
 *
 * <p>{@code enabled} commande l'existence même du module : désactivé, ses contrôleurs REST ne
 * sont pas chargés et le front ne le connaît pas. {@code show} commande sa tuile sur la page
 * d'accueil, {@code directAccess} l'accès à ses pages par URL. Ces deux derniers sont
 * facultatifs : celui qui manque se déduit de celui qui est écrit, de façon à ce qu'une
 * déclaration minimale dise déjà tout.
 *
 * <table>
 *   <caption>Déclarations possibles et réglages obtenus</caption>
 *   <tr><th>déclaré</th><th>show</th><th>directAccess</th><th>lecture</th></tr>
 *   <tr><td>rien</td><td>true</td><td>true</td><td>module pleinement ouvert</td></tr>
 *   <tr><td>{@code show: true}</td><td>true</td><td>true</td><td>idem</td></tr>
 *   <tr><td>{@code show: false}</td><td>false</td><td>false</td><td>module fermé, API servie</td></tr>
 *   <tr><td>{@code direct-access: false}</td><td>true</td><td>false</td><td>tuile affichée, pages en maintenance</td></tr>
 *   <tr><td>{@code direct-access: true}</td><td>false</td><td>true</td><td>module masqué, joignable par lien</td></tr>
 *   <tr><td>{@code enabled: false}</td><td>false</td><td>false</td><td>module absent, API comprise</td></tr>
 * </table>
 */
public record ModuleSettings(Boolean enabled, Boolean show, Boolean directAccess) {

    public ModuleSettings {
        enabled = enabled == null || enabled;

        if (!enabled) {
            /* Un module désactivé n'a ni tuile ni page, quoi qu'en disent les autres drapeaux. */
            show = false;
            directAccess = false;
        } else {
            /* L'accès suit la tuile ; et écrire `directAccess` seul n'a d'intérêt que pour le
            réglage que la tuile ne donne pas déjà, d'où la valeur opposée. */
            if (show == null) {
                show = directAccess == null || !directAccess;
            }
            if (directAccess == null) {
                directAccess = show;
            }
        }
    }
}
