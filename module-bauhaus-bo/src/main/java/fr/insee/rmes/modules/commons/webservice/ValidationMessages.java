package fr.insee.rmes.modules.commons.webservice;

/**
 * Messages des contraintes Bean Validation des corps de requête. Ils sont affichés tels quels sous
 * la saisie fautive : rédigés en français, sans nom de champ.
 */
public final class ValidationMessages {

    public static final String REQUIRED = "Ce champ est obligatoire.";

    public static final String INVALID_URL = "Cette adresse n'est pas une URL valide.";

    private ValidationMessages() {}
}
