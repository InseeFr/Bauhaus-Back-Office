package fr.insee.rmes.modules.ddi.operations_mirror.domain.model;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * L'identité DDI des objets du miroir, dérivée de l'IRI de la ressource RDF qu'ils reflètent.
 * <p>
 * Un UUID de type 3 sur l'IRI : la même série retombe toujours sur le même identifiant, donc
 * réécrire un objet le remplace (RegisterOrReplace) au lieu d'en créer un second. C'est ce qui
 * permet de traiter création et modification par le même chemin, et de rejouer une synchronisation
 * sans laisser de doublons derrière soi.
 */
public final class DdiMirrorIdentity {

    private DdiMirrorIdentity() {}

    /** L'identifiant de l'objet DDI qui reflète la ressource RDF d'IRI {@code iri}. */
    public static String of(String iri) {
        return UUID.nameUUIDFromBytes(iri.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * L'identifiant d'un objet DDI satellite de celui qui reflète {@code iri} — le LogicalProduct
     * d'une StudyUnit, sa VariableScheme —, dérivé de la même IRI et d'un suffixe qui dit son rôle.
     */
    public static String of(String iri, String role) {
        return of(iri + "#" + role);
    }

    public static String urn(String agency, String id, String version) {
        return "urn:ddi:%s:%s:%s".formatted(agency, id, version);
    }
}
